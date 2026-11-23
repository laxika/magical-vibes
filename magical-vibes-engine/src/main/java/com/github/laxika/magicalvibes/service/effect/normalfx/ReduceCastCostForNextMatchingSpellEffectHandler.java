package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.DeterminedOnce;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Floats a reduction for the controller's next matching spell. An {@link XValue}, {@link EventValue} or
 * {@link DeterminedOnce} amount is fixed as the effect resolves ("where X is ... as this ability resolves",
 * CR 608.2h); any other amount is read when that spell's cost is determined (affinity, CR 601.2f).
 */
@Component("reduceCastCostForNextMatchingSpellNormalEffectHandler")
@RequiredArgsConstructor
public class ReduceCastCostForNextMatchingSpellEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForNextMatchingSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReduceCastCostForNextMatchingSpellEffect reduction =
                (ReduceCastCostForNextMatchingSpellEffect) effect;
        Integer lockedAmount = reduction.amount() instanceof XValue
                ? Integer.valueOf(entry.getXValue())
                : reduction.amount() instanceof EventValue
                ? Integer.valueOf(entry.getEventValue())
                : reduction.amount() instanceof DeterminedOnce
                ? Integer.valueOf(Math.max(0, amountEvaluationService.evaluate(
                        gameData, reduction.amount(), AmountContext.forStackEntry(entry, livingSource(gameData, entry)))))
                : null;
        CardEffect floatingEffect = lockedAmount == null
                ? reduction
                : new ReduceCastCostForNextMatchingSpellEffect(
                        reduction.predicate(), new Fixed(lockedAmount), reduction.faceDownOnly(),
                        reduction.expiresAtCleanup());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                null,
                entry.getControllerId(),
                floatingEffect,
                null,
                null,
                null,
                Duration.UNTIL_MATCHING_SPELL_CAST,
                0
        ));
    }

    /** The source as it is now, or null to read its last-known snapshot (CR 608.2h). */
    private Permanent livingSource(GameData gameData, StackEntry entry) {
        return entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
    }
}
