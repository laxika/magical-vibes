package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Attached-scope static handler for {@link AttachedBoostEffect}: when the target matches the
 * effect's {@link com.github.laxika.magicalvibes.model.effect.GrantScope} (typically the
 * enchanted/equipped creature), evaluates the {@code DynamicAmount}s via
 * {@link AmountEvaluationService} and adds the result as a continuous bonus. Replaces the former
 * per-derivation {@code BoostCreaturePer*} handler classes.
 *
 * <p>Amounts are evaluated with the source (the Aura/Equipment) as the amount source and its
 * controller as the amount controller, so {@code CONTROLLER}-scoped counts read the attachment's
 * controller — not the enchanted/equipped creature's controller (CR 109.5). Evaluation runs under
 * {@link AmountContext#forStaticEffect} (static recursion guard), so permanent counts use only
 * intrinsic values and cannot recurse back into this computation.
 */
@Component
@RequiredArgsConstructor
public class AttachedBoostEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachedBoostEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (AttachedBoostEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        AmountContext ctx = AmountContext.forStaticEffect(context.source(), controllerId);
        accumulator.addPower(amountEvaluationService.evaluate(context.gameData(), boost.powerBoost(), ctx));
        accumulator.addToughness(amountEvaluationService.evaluate(context.gameData(), boost.toughnessBoost(), ctx));
    }
}
