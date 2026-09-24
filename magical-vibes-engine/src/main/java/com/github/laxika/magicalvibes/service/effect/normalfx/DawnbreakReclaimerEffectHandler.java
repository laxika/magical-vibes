package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.DawnbreakReclaimerChoiceStage;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.DawnbreakReclaimerContext;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DawnbreakReclaimerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DawnbreakReclaimerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DawnbreakReclaimerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DawnbreakReclaimerEffect reclaimerEffect = (DawnbreakReclaimerEffect) effect;
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;

        if (reclaimerEffect.opponentCardId() != null || reclaimerEffect.ownCardId() != null) {
            returnSelectedCards(gameData, entry, reclaimerEffect);
            return;
        }

        if (state.dawnbreakReclaimerChosenOwnCardId != null) {
            queueMayAbility(gameData, entry, reclaimerEffect,
                    state.dawnbreakReclaimerChosenOpponentCardId,
                    state.dawnbreakReclaimerChosenOwnCardId);
            return;
        }

        if (state.dawnbreakReclaimerChosenOpponentCardId != null
                || state.dawnbreakReclaimerChosenOpponentId != null) {
            beginOwnCardChoice(gameData, entry, reclaimerEffect);
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> opponentCards = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .flatMap(playerId -> gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream())
                .filter(card -> matches(card, reclaimerEffect.filter(), entry.getCard().getId()))
                .toList();

        if (!opponentCards.isEmpty()) {
            if (opponentCards.size() == 1) {
                chooseOpponentCard(gameData, entry, opponentCards.getFirst());
                beginOwnCardChoice(gameData, entry, reclaimerEffect);
            } else {
                state.resolutionTimeDawnbreakReclaimerOpponentCardChoiceResume = true;
                gameData.rerunCurrentEffectAfterInteraction = true;
                interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                                controllerId,
                                IntStream.range(0, opponentCards.size()).boxed().toList(),
                                GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                                entry.getCard().getName()
                                        + " — choose a creature card in an opponent's graveyard.")
                        .cardPool(new ArrayList<>(opponentCards))
                        .mandatory(true)
                        .build());
            }
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }
        if (opponents.size() == 1) {
            state.dawnbreakReclaimerChosenOpponentId = opponents.getFirst();
            beginOwnCardChoice(gameData, entry, reclaimerEffect);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DawnbreakReclaimerOpponentChoice());
        playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                entry.getCard().getName() + " — choose an opponent to choose a creature card in your graveyard.");
    }

    /** Completes the controller's opponent choice and resumes the parked ability. */
    public void completeOpponentChoice(GameData gameData, UUID opponentId) {
        gameData.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentId = opponentId;
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void chooseOpponentCard(GameData gameData, StackEntry entry, Card card) {
        gameData.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentCardId = card.getId();
        gameData.graveyardTargetOperation.dawnbreakReclaimerChosenOpponentId =
                gameQueryService.findGraveyardOwnerById(gameData, card.getId());
    }

    private void beginOwnCardChoice(GameData gameData, StackEntry entry,
                                    DawnbreakReclaimerEffect effect) {
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        UUID opponentId = state.dawnbreakReclaimerChosenOpponentId;
        if (opponentId == null && state.dawnbreakReclaimerChosenOpponentCardId != null) {
            opponentId = gameQueryService.findGraveyardOwnerById(
                    gameData, state.dawnbreakReclaimerChosenOpponentCardId);
            state.dawnbreakReclaimerChosenOpponentId = opponentId;
        }
        if (opponentId == null) {
            clearState(gameData);
            return;
        }

        List<Card> ownCards = matchingCards(gameData.playerGraveyards.get(entry.getControllerId()),
                effect.filter(), entry.getCard().getId());
        if (ownCards.isEmpty()) {
            queueMayAbility(gameData, entry, effect, state.dawnbreakReclaimerChosenOpponentCardId, null);
        } else if (ownCards.size() == 1) {
            queueMayAbility(gameData, entry, effect,
                    state.dawnbreakReclaimerChosenOpponentCardId, ownCards.getFirst().getId());
        } else {
            state.resolutionTimeDawnbreakReclaimerOwnCardChoiceResume = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            String controllerName = gameData.playerIdToName.get(entry.getControllerId());
            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                            opponentId,
                            IntStream.range(0, ownCards.size()).boxed().toList(),
                            GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                            entry.getCard().getName() + " — choose a creature card from "
                                    + controllerName + "'s graveyard.")
                    .cardPool(new ArrayList<>(ownCards))
                    .mandatory(true)
                    .build());
        }
    }

    private void queueMayAbility(GameData gameData, StackEntry entry,
                                 DawnbreakReclaimerEffect effect,
                                 UUID opponentCardId, UUID ownCardId) {
        if (opponentCardId == null && ownCardId == null) {
            clearState(gameData);
            return;
        }

        clearState(gameData);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), entry.getControllerId(),
                List.of(new DawnbreakReclaimerEffect(effect.filter(), opponentCardId, ownCardId)),
                "You may return those cards to the battlefield under their owners' control.",
                null, null, entry.getSourcePermanentId()));
    }

    private void returnSelectedCards(GameData gameData, StackEntry entry,
                                     DawnbreakReclaimerEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> selectedCards = new ArrayList<>();
        HashMap<UUID, UUID> graveyardOwners = new HashMap<>();
        List<UUID> selectedCardIds = new ArrayList<>(2);
        if (effect.opponentCardId() != null) {
            selectedCardIds.add(effect.opponentCardId());
        }
        if (effect.ownCardId() != null) {
            selectedCardIds.add(effect.ownCardId());
        }
        for (UUID cardId : selectedCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (card == null || graveyardOwnerId == null
                    || !matches(card, effect.filter(), entry.getCard().getId())) {
                continue;
            }
            if (cardId.equals(effect.opponentCardId()) && graveyardOwnerId.equals(controllerId)) {
                continue;
            }
            if (cardId.equals(effect.ownCardId()) && !graveyardOwnerId.equals(controllerId)) {
                continue;
            }
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
            selectedCards.add(card);
            graveyardOwners.put(cardId, graveyardOwnerId);
        }

        if (!selectedCards.isEmpty()) {
            graveyardReturnSupport.putCardsOntoBattlefieldSimultaneouslyUnderController(gameData,
                    new PendingGraveyardReturnBatch(controllerId, selectedCards, graveyardOwners, true));
        }
    }

    private List<Card> matchingCards(List<Card> cards, CardPredicate filter, UUID sourceCardId) {
        if (cards == null) {
            return List.of();
        }
        return cards.stream()
                .filter(card -> matches(card, filter, sourceCardId))
                .toList();
    }

    private boolean matches(Card card, CardPredicate filter, UUID sourceCardId) {
        return predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId);
    }

    private void clearState(GameData gameData) {
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        state.resolutionTimeDawnbreakReclaimerOpponentCardChoiceResume = false;
        state.resolutionTimeDawnbreakReclaimerOwnCardChoiceResume = false;
        state.dawnbreakReclaimerChosenOpponentId = null;
        state.dawnbreakReclaimerChosenOpponentCardId = null;
        state.dawnbreakReclaimerChosenOwnCardId = null;
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
