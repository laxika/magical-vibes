package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledLandsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Rampant Frogantua's mill and optional milled-land battlefield return. */
@Component
@RequiredArgsConstructor
public class MillControllerAndMayPutMilledLandsOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayPutMilledLandsOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndMayPutMilledLandsOntoBattlefieldEffect) effect;
        var choiceContext = gameData.graveyardTargetOperation.milledCreatureReturn;
        if (choiceContext != null && choiceContext.chosenCardIds() != null) {
            gameData.graveyardTargetOperation.milledCreatureReturn = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            returnCards(gameData, entry, choiceContext.chosenCardIds());
            return;
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, millEffect.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot())));
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), count);
        CardTypePredicate landPredicate = new CardTypePredicate(CardType.LAND);
        List<Card> eligibleCards = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, landPredicate, entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            return;
        }

        gameData.graveyardTargetOperation.milledCreatureReturn =
                new GraveyardTargetOperationState.MilledCreatureReturnContext(null);
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                eligibleCards,
                eligibleCards.size(),
                "Choose any number of land cards to put onto the battlefield tapped.");
    }

    private void returnCards(GameData gameData, StackEntry entry, List<UUID> cardIds) {
        List<UUID> previousTargetCardIds = entry.getTargetCardIds();
        entry.setTargetCardIds(cardIds);
        try {
            returnHandler.resolveForController(
                    gameData,
                    entry,
                    new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                            new CardTypePredicate(CardType.LAND), cardIds.size(), false, true),
                    entry.getControllerId());
        } finally {
            entry.setTargetCardIds(previousTargetCardIds);
        }
    }
}
