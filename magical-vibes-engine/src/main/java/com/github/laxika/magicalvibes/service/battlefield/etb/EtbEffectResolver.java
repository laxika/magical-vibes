package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.condition.EntryFactCondition;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
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
 * <p><b>Conditions.</b> A {@link ConditionalEffect} among an ability's effects is the "if" of that effect, judged as
 * it resolves; one fixed as the permanent enters (Kicked / CastFromZone, a cost paid, the mana spent, whether it is a
 * token, {@code SourceUntapped} sampled at entry) is judged now instead and <em>unwraps</em> to its inner effect, or
 * drops ({@code null}) when false. Every other condition passes through unchanged and is checked by
 * {@code EffectResolutionService}. The ability's intervening "if" (CR 603.4) and event condition (CR 603.2) are its
 * options: {@link #ownEntryConditionMet} judges them as the ability triggers, through the same cases and then the game
 * state (Metalcraft, Morbid, Raid, "if you control another …"), and the intervening "if" wraps the ability's effects
 * ({@code TriggeredAbility.ownEntryEffects()}), so it reaches this handler too and is checked again as the ability
 * resolves. Only a condition on the ability's target, which isn't chosen yet, passes through unchecked.
 */
@Component
public class EtbEffectResolver {

    private final Map<Class<? extends CardEffect>, EtbEffectHandler> handlers = new HashMap<>();
    private final ConditionEvaluationService conditionEvaluationService;

    public EtbEffectResolver(ConditionEvaluationService conditionEvaluationService) {
        this.conditionEvaluationService = conditionEvaluationService;
        // Tribute's "if tribute wasn't paid" ability is an intervening-if trigger. The choice is
        // recorded on the entering permanent before this resolver runs.
        register(TributeNotPaidEffect.class, (ctx, effect) -> {
            TributeNotPaidEffect tribute = (TributeNotPaidEffect) effect;
            return ctx.sourcePermanent() != null && !ctx.sourcePermanent().isTributePaid()
                    ? tribute.wrapped() : null;
        });

        // Evoke sacrifice (CR 702.74a) — intervening-if (CR 603.4): resolve to a plain sacrifice
        // when the evoke cost was paid, otherwise drop the trigger entirely.
        register(SacrificeSelfIfEvokedEffect.class, (ctx, effect) ->
                ctx.evoked() ? new SacrificeSelfEffect(true) : null);

        // Dash's return to hand is queued when the permanent enters from a dash cast, so it is not an
        // enters-the-battlefield trigger and never goes on the stack.
        register(ReturnSelfToHandIfDashCostPaidEffect.class, (ctx, effect) -> null);

        // Conditional ETB effects: immutable cast-time conditions are evaluated while the trigger
        // is created, gate types (Metalcraft / Morbid / Raid / ControlsAnother) stay wrapped for
        // re-evaluation at stack resolution, and source-untapped clauses are sampled at entry.
        register(ConditionalEffect.class, (ctx, effect) -> {
            ConditionalEffect conditional = (ConditionalEffect) effect;
            return switch (verdict(ctx, conditional)) {
                case DROP -> null;
                case NOTHING -> new DrawCardEffect(0);
                case UNWRAP -> conditional.wrapped();
                case UNJUDGED -> effect;
            };
        });
    }

    /**
     * What becomes of a {@link ConditionalEffect} of a "when this enters" ability as the ability triggers: a fact fixed
     * as the permanent entered ({@link EntryFactCondition}: a kicker, the zone it was cast from, a cost paid, the mana
     * spent) is judged now, as the stack entry can't read how the permanent was cast, and unwraps when true
     * ({@link #UNWRAP}) or leaves an effect that does nothing in its place so the ability still goes on the stack
     * ({@link #NOTHING}, CR 603.4: an effect's "if" doesn't keep its ability from triggering); any other condition is
     * kept wrapped ({@link #UNJUDGED}) and checked as the effect resolves. Blitz's reminder abilities drop
     * ({@link #DROP}).
     */
    private enum Verdict {
        DROP, NOTHING, UNWRAP, UNJUDGED
    }

    /** What becomes of {@code conditional} as the ability it is part of triggers ({@link Verdict}). */
    private Verdict verdict(EtbEffectContext ctx, ConditionalEffect conditional) {
        if (conditional.condition() instanceof CastForAlternateCost && isBlitzReminder(conditional.wrapped())) {
            return Verdict.DROP;
        }
        if (!EntryFactCondition.isEntryFact(conditional.condition())) {
            return Verdict.UNJUDGED;
        }
        return conditionEvaluationService.isMet(ctx.gameData(), conditional.condition(), conditionContext(ctx))
                ? Verdict.UNWRAP : Verdict.NOTHING;
    }

    /**
     * Blitz's "sacrifice it at the beginning of the next end step" and haste (CR 702.152a): a delayed trigger and a
     * static ability the spell's resolution puts in place, not effects of an enters ability.
     */
    private static boolean isBlitzReminder(CardEffect wrapped) {
        return wrapped instanceof SacrificeSelfAtEndStepEffect
                || wrapped instanceof com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect grant
                && grant.scope() == com.github.laxika.magicalvibes.model.effect.GrantScope.SELF
                && grant.keywords().equals(java.util.Set.of(com.github.laxika.magicalvibes.model.Keyword.HASTE));
    }

    /**
     * Whether a "when this enters" ability whose intervening "if" (CR 603.4) or event condition (CR 603.2) is
     * {@code condition} triggers as the permanent enters, judged with the entering permanent's cast-time facts; a
     * condition on the ability's target, which isn't chosen yet (CR 603.3d), is judged as it resolves, but an Aura's,
     * whose enchanted object is known as it enters.
     */
    public boolean ownEntryConditionMet(EtbEffectContext ctx, Condition condition) {
        if (condition instanceof TargetPermanentMatches && !(ctx.targetId() != null && ctx.card().isAura())) {
            return true;
        }
        return conditionEvaluationService.isMet(ctx.gameData(), condition, conditionContext(ctx));
    }

    /** The context the conditions of {@code ctx}'s entering permanent are read in, with its cast-time facts. */
    private static ConditionContext conditionContext(EtbEffectContext ctx) {
        Zone sourceZone = ctx.sourcePermanent() == null
                ? (ctx.wasCastFromHand() ? Zone.HAND : null)
                : (ctx.sourcePermanent().isCast() ? ctx.sourcePermanent().getCastFromZone() : null);
        boolean collectEvidenceCostPaid = ctx.sourcePermanent() != null
                && ctx.sourcePermanent().isCollectEvidenceCostPaid();
        Card sacrificedCard = ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getCastSacrificedCard();
        return new ConditionContext(ctx.controllerId(),
                ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId(),
                ctx.sourcePermanent(), ctx.card(), ctx.kicked(), false, ctx.prowl(), ctx.madness(), false, false,
                sourceZone, ctx.xValue(), ctx.targetId(), null, false, false, false, null, null, sacrificedCard,
                ctx.repeatedAdditionalCosts(), ctx.alternateCost(),
                ctx.sourcePermanent() != null && ctx.sourcePermanent().isSpectacle(),
                false, collectEvidenceCostPaid, false, 0, false);
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
