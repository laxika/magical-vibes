package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardOntoBattlefieldUnderControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Gyruda's mill and mandatory creature-card choice. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;
    private final PutExiledCardOntoBattlefieldUnderControllerEffectHandler putExiledHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect) effect;
        var choiceContext = gameData.graveyardTargetOperation.milledCreatureReturn;
        if (choiceContext != null && choiceContext.chosenCardIds() != null) {
            gameData.graveyardTargetOperation.milledCreatureReturn = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            putChosenCard(gameData, entry, choiceContext.chosenCardIds());
            return;
        }

        List<Card> milled = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            milled.addAll(graveyardService.resolveMillPlayerIncludingExiled(
                    gameData, playerId, millEffect.count()));
        }

        List<Card> eligibleCards = milled.stream()
                .filter(this::isEligible)
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
                "Choose a creature card with an even mana value to put onto the battlefield.");
    }

    private void putChosenCard(GameData gameData, StackEntry entry, List<UUID> chosenCardIds) {
        if (chosenCardIds.isEmpty()) {
            return;
        }
        UUID chosenCardId = chosenCardIds.getFirst();
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, chosenCardId);
        if (graveyardCard != null && isEligible(graveyardCard)) {
            List<UUID> previousTargetCardIds = entry.getTargetCardIds();
            entry.setTargetCardIds(List.of(chosenCardId));
            try {
                returnHandler.resolve(
                        gameData,
                        entry,
                        ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyards(
                                new CardTypePredicate(CardType.CREATURE)));
            } finally {
                entry.setTargetCardIds(previousTargetCardIds);
            }
            return;
        }

        Card exiledCard = gameData.findExiledCard(chosenCardId) == null
                ? null
                : gameData.findExiledCard(chosenCardId).card();
        if (isEligible(exiledCard)) {
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

    private boolean isEligible(Card card) {
        return card != null && card.hasType(CardType.CREATURE) && card.getManaValue() % 2 == 0;
    }
}
