package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Resolves Eivor's mill and optional Saga/land battlefield return. */
@Component
@RequiredArgsConstructor
public class MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect) effect;
        var context = gameData.graveyardTargetOperation.milledSagaAndLandReturn;
        if (context != null) {
            if (context.awaitingChoice()) {
                return;
            }
            context = new GraveyardTargetOperationState.MilledSagaAndLandReturnContext(
                    context.sagaCardIds(), context.landCardIds(), context.categoryIndex() + 1,
                    context.selectedCardIds(), false);
            gameData.graveyardTargetOperation.milledSagaAndLandReturn = context;
            continueChoiceOrReturn(gameData, entry, context);
            return;
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, millEffect.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot())));
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), count);
        CardSubtypePredicate sagaPredicate = new CardSubtypePredicate(CardSubtype.SAGA);
        CardTypePredicate landPredicate = new CardTypePredicate(CardType.LAND);
        List<UUID> sagaCardIds = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, sagaPredicate, entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .map(Card::getId)
                .toList();
        List<UUID> landCardIds = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, landPredicate, entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .map(Card::getId)
                .toList();

        context = new GraveyardTargetOperationState.MilledSagaAndLandReturnContext(
                sagaCardIds, landCardIds, 0, List.of(), false);
        gameData.graveyardTargetOperation.milledSagaAndLandReturn = context;
        continueChoiceOrReturn(gameData, entry, context);
    }

    private void continueChoiceOrReturn(GameData gameData, StackEntry entry,
                                        GraveyardTargetOperationState.MilledSagaAndLandReturnContext context) {
        int categoryIndex = context.categoryIndex();
        while (categoryIndex < 2) {
            List<UUID> candidateIds = categoryIndex == 0 ? context.sagaCardIds() : context.landCardIds();
            List<Card> candidates = candidateIds.stream()
                    .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                    .filter(Objects::nonNull)
                    .toList();
            if (!candidates.isEmpty()) {
                gameData.graveyardTargetOperation.milledSagaAndLandReturn =
                        new GraveyardTargetOperationState.MilledSagaAndLandReturnContext(
                                context.sagaCardIds(), context.landCardIds(), categoryIndex,
                                context.selectedCardIds(), true);
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates, 1,
                        "Choose up to one " + (categoryIndex == 0 ? "Saga card" : "land card")
                                + " to put onto the battlefield.");
                return;
            }
            categoryIndex++;
            context = new GraveyardTargetOperationState.MilledSagaAndLandReturnContext(
                    context.sagaCardIds(), context.landCardIds(), categoryIndex,
                    context.selectedCardIds(), false);
            gameData.graveyardTargetOperation.milledSagaAndLandReturn = context;
        }

        gameData.graveyardTargetOperation.milledSagaAndLandReturn = null;
        gameData.rerunCurrentEffectAfterInteraction = false;
        List<Card> selectedCards = context.selectedCardIds().stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .filter(Objects::nonNull)
                .toList();
        if (selectedCards.isEmpty()) {
            return;
        }
        selectedCards.forEach(card -> permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId()));
        graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                gameData, Map.of(entry.getControllerId(), selectedCards), false, null);
    }
}
