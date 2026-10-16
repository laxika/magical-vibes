package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsOneThenDrawsForEachCardTypeState;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves Kefka's each-player discard followed by a draw for each card type among the discarded
 * cards. Each player chooses a card in APNAP order (CR 101.4); the chosen cards are then discarded
 * simultaneously (CR 608.2f), and the controller draws one card per distinct card type among the
 * cards that actually reached the graveyard.
 *
 * <p>Selections are deferred through {@link EachPlayerRummageState}, and the handler re-runs on each
 * completed selection via {@code rerunCurrentEffectAfterInteraction}. The distinct card types are
 * kept in {@link EachPlayerDiscardsOneThenDrawsForEachCardTypeState} so the draw amount can be
 * evaluated from them.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsOneThenDrawsForEachCardTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final ObjectProvider<CardChoiceHandlerService> cardChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerRummageState rummage = gameData.eachPlayerRummage;

        if (!rummage.active) {
            rummage.reset();
            rummage.active = true;
            rummage.deferDiscards = true;
            if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
                rummage.remaining.addLast(gameData.activePlayerId);
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    rummage.remaining.addLast(playerId);
                }
            }
        }

        beginNextDiscard(gameData, entry, effect);
    }

    private void beginNextDiscard(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerRummageState rummage = gameData.eachPlayerRummage;
        UUID controllerId = entry.getControllerId();

        while (!rummage.remaining.isEmpty()) {
            UUID playerId = rummage.remaining.pollFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty() || !gameQueryService.canEffectCauseDiscard(gameData, playerId, controllerId)) {
                continue;
            }

            rummage.currentPlayerId = playerId;
            gameData.discardCausedByOpponent = !playerId.equals(controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            rummage.currentPlayerId = null;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        finishDiscards(gameData, entry, effect);
    }

    private void finishDiscards(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerRummageState rummage = gameData.eachPlayerRummage;
        UUID controllerId = entry.getControllerId();
        Map<UUID, Card> chosenCards = new HashMap<>();
        for (EachPlayerRummageState.SelectedDiscard selection : rummage.selectedDiscards) {
            gameData.playerHands.getOrDefault(selection.playerId(), List.of()).stream()
                    .filter(card -> card.getId().equals(selection.cardId()))
                    .findFirst()
                    .ifPresent(card -> chosenCards.put(selection.cardId(), card));
        }

        cardChoiceHandlerServiceProvider.getObject().discardCollectedCards(
                gameData, List.copyOf(rummage.selectedDiscards), controllerId);

        EachPlayerDiscardsOneThenDrawsForEachCardTypeState typeState =
                gameData.eachPlayerDiscardsOneThenDrawsForEachCardType;
        typeState.discardedCardTypes.clear();
        for (EachPlayerRummageState.SelectedDiscard selection : rummage.selectedDiscards) {
            Card card = chosenCards.get(selection.cardId());
            if (card != null && isInGraveyard(gameData, selection.playerId(), card.getId())) {
                EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
                if (card.getType() != null) {
                    cardTypes.add(card.getType());
                }
                cardTypes.addAll(card.getAdditionalTypes());
                typeState.discardedCardTypes.addAll(cardTypes);
            }
        }

        rummage.reset();
        int drawAmount = amountEvaluationService.evaluate(gameData,
                ((EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect) effect).drawnCardAmount(),
                AmountContext.forStackEntry(entry, null));
        typeState.discardedCardTypes.clear();
        if (drawAmount > 0) {
            playerInteractionSupport.applyDrawCards(gameData, controllerId, drawAmount);
        }
    }

    private boolean isInGraveyard(GameData gameData, UUID playerId, UUID cardId) {
        return gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                .anyMatch(card -> card.getId().equals(cardId));
    }
}
