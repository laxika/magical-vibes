package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardsToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MillControllerAndReturnMilledCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndReturnMilledCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndReturnMilledCardsToHandEffect) effect;
        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return;
        }
        List<Card> returnable = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, millEffect.filter(), entry.getCard().getId(), gameData, entry.getControllerId()))
                .filter(graveyard::contains)
                .toList();
        if (returnable.isEmpty()) {
            return;
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : returnable) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                gameData.addCardToHand(entry.getControllerId(), card);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
    }
}
