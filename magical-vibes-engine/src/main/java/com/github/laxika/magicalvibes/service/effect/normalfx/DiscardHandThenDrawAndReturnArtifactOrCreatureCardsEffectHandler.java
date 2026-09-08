package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.QueenKaylaBinKroogOperationState;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        QueenKaylaBinKroogOperationState state = gameData.queenKaylaBinKroogOperation;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        gameData.rerunCurrentEffectAfterInteraction = false;
        if (!state.active) {
            state.active = true;
            state.controllerId = controllerId;

            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Card> discarded = hand == null ? List.of() : new ArrayList<>(hand);
            if (hand != null) {
                hand.clear();
            }
            gameData.discardCausedByOpponent = false;
            for (Card card : discarded) {
                state.discardedCardIds.add(card.getId());
                graveyardService.discardCard(gameData, controllerId, card);
                triggerCollectionService.checkDiscardTriggers(gameData, controllerId, card);
            }

            String discardLog = playerName + " discards their hand (" + discarded.size()
                    + " card" + (discarded.size() != 1 ? "s" : "") + ") (" + cardName + ").";
            gameLogService.append(gameData, GameLog.text(discardLog));

            for (int i = 0; i < discarded.size(); i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            String drawLog = playerName + " draws " + discarded.size()
                    + " card" + (discarded.size() != 1 ? "s" : "") + ".";
            gameLogService.append(gameData, GameLog.text(drawLog));
        }

        if (state.choiceMade) {
            if (state.chosenCardId != null && !state.chosenCardIds.contains(state.chosenCardId)) {
                state.chosenCardIds.add(state.chosenCardId);
            }
            state.choiceMade = false;
            state.chosenCardId = null;
            state.nextManaValue++;
        }

        while (state.nextManaValue <= 3) {
            List<Card> candidates = candidatesFor(gameData, state, state.nextManaValue);
            if (candidates.isEmpty()) {
                state.nextManaValue++;
                continue;
            }

            state.awaitingChoice = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            List<Integer> validIndices = java.util.stream.IntStream.range(0, candidates.size()).boxed().toList();
            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                            controllerId,
                            validIndices,
                            GraveyardChoiceDestination.BATTLEFIELD,
                            "You may choose an artifact or creature card with mana value "
                                    + state.nextManaValue + " discarded this way.")
                    .cardPool(candidates)
                    .build());
            return;
        }

        List<Card> graveyardCards = new ArrayList<>();
        List<Card> exiledCards = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        for (UUID cardId : state.chosenCardIds) {
            Card card = null;
            if (graveyard != null) {
                card = graveyard.stream().filter(candidate -> candidate.getId().equals(cardId)).findFirst().orElse(null);
            }
            if (card != null) {
                graveyard.remove(card);
                graveyardCards.add(card);
            } else {
                var exiled = gameData.findExiledCard(cardId);
                if (exiled != null && gameData.removeFromExile(cardId)) {
                    exiledCards.add(exiled.card());
                }
            }
        }

        if (!graveyardCards.isEmpty()) {
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, graveyardCards);
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
        }
        graveyardReturnSupport.putCardsFromGraveyardAndExileOntoBattlefieldSimultaneously(
                gameData, controllerId, graveyardCards, exiledCards);
        state.reset();
    }

    private List<Card> candidatesFor(GameData gameData, QueenKaylaBinKroogOperationState state, int manaValue) {
        List<Card> candidates = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(state.controllerId, List.of());
        for (UUID cardId : state.discardedCardIds) {
            if (state.chosenCardIds.contains(cardId)) {
                continue;
            }
            Card card = graveyard.stream().filter(candidate -> candidate.getId().equals(cardId)).findFirst().orElse(null);
            if (card == null) {
                var exiled = gameData.findExiledCard(cardId);
                card = exiled == null ? null : exiled.card();
            }
            if (card != null && card.getManaValue() == manaValue
                    && (card.hasType(CardType.ARTIFACT) || card.hasType(CardType.CREATURE))) {
                candidates.add(card);
            }
        }
        return candidates;
    }
}
