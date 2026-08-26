package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCreaturesOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a mill followed by choosing a capped number of the milled creatures to reanimate. */
@Component
@RequiredArgsConstructor
public class MillControllerAndPutMilledCreaturesOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndPutMilledCreaturesOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndPutMilledCreaturesOntoBattlefieldEffect) effect;
        var choiceContext = gameData.graveyardTargetOperation.milledCreatureReturn;
        if (choiceContext != null && choiceContext.chosenCardIds() != null) {
            gameData.graveyardTargetOperation.milledCreatureReturn = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            returnCards(gameData, entry, choiceContext.chosenCardIds(), millEffect.maxCount());
            return;
        }

        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());
        CardTypePredicate creaturePredicate = new CardTypePredicate(CardType.CREATURE);
        List<Card> eligibleCards = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, creaturePredicate, entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            return;
        }

        gameData.graveyardTargetOperation.milledCreatureReturn =
                new com.github.laxika.magicalvibes.model.GraveyardTargetOperationState
                        .MilledCreatureReturnContext(null);
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                eligibleCards,
                millEffect.maxCount(),
                "Choose up to " + millEffect.maxCount()
                        + " creature cards to put onto the battlefield.");
    }

    private void returnCards(GameData gameData, StackEntry entry, List<UUID> cardIds, int maxCount) {
        List<UUID> previousTargetCardIds = entry.getTargetCardIds();
        entry.setTargetCardIds(cardIds);
        try {
            returnHandler.resolveForController(
                    gameData,
                    entry,
                    new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                            new CardTypePredicate(CardType.CREATURE), maxCount, false, false),
                    entry.getControllerId());
        } finally {
            entry.setTargetCardIds(previousTargetCardIds);
        }
    }
}
