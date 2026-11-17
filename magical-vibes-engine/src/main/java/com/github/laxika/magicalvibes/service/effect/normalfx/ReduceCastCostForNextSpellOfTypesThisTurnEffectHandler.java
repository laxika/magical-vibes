package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextSpellOfTypesThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForNextSpellOfTypesThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForNextSpellOfTypesThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reduction = (ReduceCastCostForNextSpellOfTypesThisTurnEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = Math.max(0, amountEvaluationService.evaluate(
                gameData, reduction.amount(), AmountContext.forStackEntry(entry, source)));
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(), null,
                entry.getControllerId(),
                new ReduceCastCostForNextMatchingSpellEffect(new CardAnyOfPredicate(reduction.cardTypes().stream()
                        .<CardPredicate>map(CardTypePredicate::new).toList()), amount),
                null, null, null, Duration.UNTIL_MATCHING_SPELL_CAST, 0));
    }
}
