package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffect) effect;
        var choiceContext = gameData.graveyardTargetOperation.milledCreaturesToHand;
        if (choiceContext != null && choiceContext.chosenCardIds() != null) {
            gameData.graveyardTargetOperation.milledCreaturesToHand = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            returnCardsToHand(gameData, entry, choiceContext.chosenCardIds());
            return;
        }

        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());
        List<Card> eligibleCards = milled.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            return;
        }

        gameData.graveyardTargetOperation.milledCreaturesToHand =
                new GraveyardTargetOperationState.MilledCreaturesToHandContext(null);
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                eligibleCards,
                eligibleCards.size(),
                "Choose any number of creature cards to put into your hand.");
    }

    private void returnCardsToHand(GameData gameData, StackEntry entry, List<UUID> cardIds) {
        List<Card> cards = cardIds.stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .filter(card -> card != null && card.hasType(CardType.CREATURE))
                .toList();
        if (cards.isEmpty()) {
            return;
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : cards) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                permanentRemovalService.addCardToHandFromGraveyard(
                        gameData, entry.getControllerId(), entry.getControllerId(), card);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
    }
}
