package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.DawnbreakReclaimerChoiceStage;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.DawnbreakReclaimerContext;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DawnbreakReclaimerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DawnbreakReclaimerEffectHandler implements NormalEffectHandlerBean {

    private static final CardTypePredicate CREATURE_CARD = new CardTypePredicate(CardType.CREATURE);

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DawnbreakReclaimerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var state = gameData.graveyardTargetOperation;
        DawnbreakReclaimerContext context = state.dawnbreakReclaimer;

        if (context != null && context.stage() == DawnbreakReclaimerChoiceStage.READY
                && gameData.resolvedMayAccepted != null) {
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (accepted) {
                returnChosenCards(gameData, entry, context);
            }
            state.dawnbreakReclaimer = null;
            return;
        }

        if (context == null) {
            List<Card> opponentCards = opponentCreatureCards(
                    gameData, entry.getControllerId(), entry.getCard().getId());
            if (opponentCards.isEmpty()) {
                return;
            }
            if (opponentCards.size() > 1) {
                state.dawnbreakReclaimer = new DawnbreakReclaimerContext(
                        null, null, null, DawnbreakReclaimerChoiceStage.OPPONENT_CARD);
                beginChoice(gameData, entry.getControllerId(), opponentCards,
                        entry.getCard().getName() + " — choose a creature card in an opponent's graveyard.");
                return;
            }
            context = contextAfterOpponentChoice(gameData, opponentCards.getFirst());
            if (context == null) {
                return;
            }
            state.dawnbreakReclaimer = context;
        }

        if (context.stage() == DawnbreakReclaimerChoiceStage.CONTROLLER_CARD) {
            List<Card> controllerCards = creatureCards(
                    gameData, entry.getControllerId(), entry.getCard().getId());
            if (controllerCards.size() > 1) {
                beginChoice(gameData, context.opponentPlayerId(), controllerCards,
                        entry.getCard().getName() + " — choose a creature card in your graveyard.");
                return;
            }
            Card chosen = controllerCards.isEmpty() ? null : controllerCards.getFirst();
            context = new DawnbreakReclaimerContext(
                    context.opponentCardId(), context.opponentPlayerId(),
                    chosen == null ? null : chosen.getId(), DawnbreakReclaimerChoiceStage.READY);
            state.dawnbreakReclaimer = context;
        }

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), entry.getControllerId(), List.of(effect),
                entry.getCard().getName()
                        + " — Return those cards to the battlefield under their owners' control?"));
    }

    private void beginChoice(GameData gameData, UUID chooserId, List<Card> cards, String prompt) {
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(chooserId, IntStream.range(0, cards.size()).boxed().toList(),
                        GraveyardChoiceDestination.MAY_ABILITY_TARGET, prompt)
                .cardPool(new ArrayList<>(cards))
                .mandatory(true)
                .build());
        gameData.rerunCurrentEffectAfterInteraction = true;
    }

    private DawnbreakReclaimerContext contextAfterOpponentChoice(GameData gameData, Card card) {
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (graveyardOwnerId == null) {
            return null;
        }
        return new DawnbreakReclaimerContext(
                card.getId(), graveyardOwnerId, null, DawnbreakReclaimerChoiceStage.CONTROLLER_CARD);
    }

    private List<Card> opponentCreatureCards(GameData gameData, UUID controllerId, UUID sourceCardId) {
        List<Card> cards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                cards.addAll(creatureCards(gameData, playerId, sourceCardId));
            }
        }
        return cards;
    }

    private List<Card> creatureCards(GameData gameData, UUID playerId, UUID sourceCardId) {
        if (playerId == null) {
            return List.of();
        }
        return gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, CREATURE_CARD, sourceCardId))
                .toList();
    }

    private void returnChosenCards(GameData gameData, StackEntry entry, DawnbreakReclaimerContext context) {
        List<Card> cards = new ArrayList<>();
        Map<UUID, UUID> graveyardOwners = new HashMap<>();
        List<UUID> cardIds = new ArrayList<>();
        cardIds.add(context.opponentCardId());
        if (context.controllerCardId() != null) {
            cardIds.add(context.controllerCardId());
        }

        for (UUID cardId : cardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card == null) {
                continue;
            }
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (graveyardOwnerId == null) {
                continue;
            }
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
            cards.add(card);
            graveyardOwners.put(cardId, graveyardOwnerId);
        }

        if (!cards.isEmpty()) {
            graveyardReturnSupport.putCardsOntoBattlefieldSimultaneouslyUnderController(
                    gameData, new PendingGraveyardReturnBatch(entry.getControllerId(), cards, graveyardOwners, true));
        }
    }
}
