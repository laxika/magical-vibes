package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndPutMilledCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardOntoBattlefieldUnderControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a multi-player mill followed by a choice of one matching milled card. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerAndPutMilledCardOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;
    private final PutExiledCardOntoBattlefieldUnderControllerEffectHandler putExiledHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerAndPutMilledCardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillEachPlayerAndPutMilledCardOntoBattlefieldEffect) effect;
        var choiceContext = gameData.graveyardTargetOperation.milledCreatureReturn;
        if (choiceContext != null && choiceContext.chosenCardIds() != null) {
            gameData.graveyardTargetOperation.milledCreatureReturn = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            putChosenCard(gameData, entry, millEffect, choiceContext.chosenCardIds());
            return;
        }

        List<Card> milled = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            milled.addAll(graveyardService.resolveMillPlayerIncludingExiled(
                    gameData, playerId, millEffect.count()));
        }

        List<Card> eligibleCards = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, millEffect.filter(), entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(card -> isStillAvailable(gameData, card))
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
                1,
                1,
                "Choose a milled permanent card to put onto the battlefield.");
    }

    private void putChosenCard(GameData gameData, StackEntry entry,
                               MillEachPlayerAndPutMilledCardOntoBattlefieldEffect effect,
                               List<UUID> chosenCardIds) {
        if (chosenCardIds.isEmpty()) {
            return;
        }
        UUID chosenCardId = chosenCardIds.getFirst();
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, chosenCardId);
        if (graveyardCard != null && isEligible(gameData, entry, effect, graveyardCard)) {
            List<UUID> previousTargetCardIds = entry.getTargetCardIds();
            entry.setTargetCardIds(List.of(chosenCardId));
            try {
                returnHandler.resolve(
                        gameData,
                        entry,
                        ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyards(effect.filter()));
            } finally {
                entry.setTargetCardIds(previousTargetCardIds);
            }
            return;
        }

        var exiledCard = gameData.findExiledCard(chosenCardId);
        if (exiledCard != null && isEligible(gameData, entry, effect, exiledCard.card())) {
            putExiledHandler.resolve(
                    gameData,
                    entry,
                    new PutExiledCardOntoBattlefieldUnderControllerEffect(chosenCardId));
        }
    }

    private boolean isStillAvailable(GameData gameData, Card card) {
        return gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null
                || gameData.findExiledCard(card.getId()) != null;
    }

    private boolean isEligible(GameData gameData, StackEntry entry,
                               MillEachPlayerAndPutMilledCardOntoBattlefieldEffect effect,
                               Card card) {
        return predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(), entry.getCard().getId(), gameData, entry.getControllerId());
    }
}
