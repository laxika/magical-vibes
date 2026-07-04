package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that resolves a creature's own mandatory enter-the-battlefield effects into the form they
 * take at trigger time — replacing the {@code instanceof} cascade that used to conflate modal unwrap,
 * value materialisation, and intervening-if gating in {@code BattlefieldEntryService}.
 *
 * <p>Each concrete {@link CardEffect} class is mapped to a small {@link EtbEffectHandler}. Effects with
 * no registered handler pass through unchanged (the default identity). {@link #resolve} performs a
 * single exact-class lookup: this collapses the former two-pass "map (unwrap) then filter (gate)"
 * pipeline into one pass, which is behaviour-identical for every real effect because each effect
 * matches exactly one branch — a raw effect is either an unwrap type, a gate type, or neither, but
 * never both (no card nests a gate conditional inside a modal/kicked wrapper).
 *
 * <p><b>Gate vs. unwrap asymmetry (preserved deliberately):</b> Metalcraft / Morbid / Raid /
 * ControlsAnother gates return the <em>conditional effect unchanged</em> when met (it stays wrapped
 * and is re-evaluated at stack resolution by {@code EffectResolutionService}), whereas Kicked /
 * CastFromZone <em>unwrap</em> to their inner effect. Conditions with no ETB policy pass through
 * unchanged. Dropping ({@code null}) applies the intervening-if rule (CR 603.4): the ability never
 * goes on the stack.
 */
@Component
public class EtbEffectResolver {

    private final Map<Class<? extends CardEffect>, EtbEffectHandler> handlers = new HashMap<>();

    public EtbEffectResolver(ConditionEvaluationService conditionEvaluationService) {
        // "you lose the game unless this was cast from your hand" — no-op (drop) when cast from hand,
        // otherwise materialise into the controller losing the game.
        register(LoseGameIfNotCastFromHandEffect.class, (ctx, effect) ->
                ctx.wasCastFromHand() ? null : new TargetPlayerLosesGameEffect(ctx.controllerId()));

        // Modal ETB (choose one / choose up to one): unwrap the option picked at cast time (etbMode).
        // Optional modals with etbMode < 0 chose no mode and drop the trigger.
        register(ChooseOneEffect.class, (ctx, effect) -> {
            ChooseOneEffect coe = (ChooseOneEffect) effect;
            if (coe.optional() && ctx.etbMode() < 0) {
                return null;
            }
            if (ctx.etbMode() >= 0 && ctx.etbMode() < coe.options().size()) {
                return coe.options().get(ctx.etbMode()).effect();
            }
            return coe.options().getFirst().effect();
        });

        // "Gain life equal to that creature's toughness" — read toughness at trigger time.
        register(GainLifeEqualToToughnessEffect.class, (ctx, effect) ->
                new GainLifeEffect(ctx.card().getToughness()));

        // Conditional ETB effects: unwrap types (Kicked / CastFromZone) resolve to the inner effect
        // when met, gate types (Metalcraft / Morbid / Raid / ControlsAnother) stay wrapped for
        // re-evaluation at stack resolution, and every other condition passes through unchanged.
        register(ConditionalEffect.class, (ctx, effect) -> {
            ConditionalEffect conditional = (ConditionalEffect) effect;
            ConditionContext conditionContext = new ConditionContext(ctx.controllerId(), null, null,
                    ctx.card(), ctx.kicked(), ctx.wasCastFromHand() ? Zone.HAND : null, 0, null, null, false);
            return switch (conditional.condition()) {
                // Kicked intervening-if (CR 603.4): unwrap when kicked, otherwise drop.
                case Kicked ignored -> ctx.kicked() ? conditional.wrapped() : null;
                // Cast-from-hand intervening-if (CR 603.4): unwrap only when cast from hand, otherwise drop.
                case CastFromZone castFromZone ->
                        conditionEvaluationService.isMet(ctx.gameData(), castFromZone, conditionContext)
                                ? conditional.wrapped() : null;
                // Intervening-if gates (CR 603.4): keep the conditional effect when met (re-checked at
                // stack resolution), drop it when not.
                case Metalcraft ignored ->
                        conditionEvaluationService.isMet(ctx.gameData(), conditional.condition(), conditionContext)
                                ? effect : null;
                case Morbid ignored ->
                        conditionEvaluationService.isMet(ctx.gameData(), conditional.condition(), conditionContext)
                                ? effect : null;
                case Raid ignored ->
                        conditionEvaluationService.isMet(ctx.gameData(), conditional.condition(), conditionContext)
                                ? effect : null;
                case ControlsAnotherPermanent ignored ->
                        conditionEvaluationService.isMet(ctx.gameData(), conditional.condition(), conditionContext)
                                ? effect : null;
                default -> effect;
            };
        });
    }

    private void register(Class<? extends CardEffect> effectClass, EtbEffectHandler handler) {
        handlers.put(effectClass, handler);
    }

    /**
     * Resolves a single mandatory ETB effect into its trigger-time form.
     *
     * @return the resolved effect to queue, or {@code null} if the trigger should be dropped
     *         (intervening-if failed, or a conditional no-op)
     */
    public CardEffect resolve(EtbEffectContext ctx, CardEffect effect) {
        EtbEffectHandler handler = handlers.get(effect.getClass());
        return handler != null ? handler.resolve(ctx, effect) : effect;
    }
}
