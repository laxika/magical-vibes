package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndReturnMilledCardsToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndReturnMilledCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndReturnMilledCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillTargetPlayerAndReturnMilledCardsToHandEffect) effect;
        List<UUID> targetPlayerIds = entry.targetsForEffect(effect);
        if (targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = Collections.singletonList(entry.getTargetId());
        }

        for (UUID targetPlayerId : targetPlayerIds) {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, targetPlayerId, e.count());
            List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
            if (graveyard == null) {
                continue;
            }
            List<Card> returnable = milled.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, e.filter(), entry.getCard().getId(), gameData, targetPlayerId))
                    .filter(graveyard::contains)
                    .toList();
            if (returnable.isEmpty()) {
                continue;
            }

            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : returnable) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                    permanentRemovalService.addCardToHandFromGraveyard(
                            gameData, targetPlayerId, targetPlayerId, card);
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
        }
    }
}
