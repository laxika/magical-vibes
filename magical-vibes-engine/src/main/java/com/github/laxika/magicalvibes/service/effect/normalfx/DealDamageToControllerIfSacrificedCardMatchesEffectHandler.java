package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a controller-damage rider based on the card sacrificed during the same ability. */
@Component
@RequiredArgsConstructor
public class DealDamageToControllerIfSacrificedCardMatchesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToControllerIfSacrificedCardMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damage = (DealDamageToControllerIfSacrificedCardMatchesEffect) effect;
        Card sacrificed = entry.getSacrificedPermanentSnapshot() == null
                ? entry.getSacrificedCardSnapshot()
                : entry.getSacrificedPermanentSnapshot().getCard();
        if (sacrificed == null && entry.getSacrificedCardId() != null) {
            sacrificed = gameQueryService.findCardInGraveyardById(gameData, entry.getSacrificedCardId());
            if (sacrificed == null) {
                sacrificed = gameQueryService.findCardInExileById(gameData, entry.getSacrificedCardId());
            }
        }
        if (sacrificed == null
                || !predicateEvaluationService.matchesCardPredicate(
                        sacrificed, damage.filter(), entry.getCard().getId())) {
            return;
        }

        dealDamageToPlayersEffectHandler.resolve(
                gameData, entry, new DealDamageToPlayersEffect(damage.amount(), DamageRecipient.CONTROLLER));
    }
}
