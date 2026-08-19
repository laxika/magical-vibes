package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * A generic conditional wrapper around a {@link CardEffect} that only applies when the
 * given {@link Condition} is met (e.g. metalcraft, equipped, morbid).
 * <p>
 * Targeting delegates to the wrapped effect so target selection works at cast time.
 * Condition evaluation is handled externally by the engine's
 * {@code ConditionEvaluationService} since domain records cannot depend on game services.
 * <p>
 * {@code interveningIf} distinguishes the two card templates that both read as "conditional"
 * but trigger differently. The default, {@code true}, is the intervening-"if" of CR 603.4
 * ("At the beginning of your end step, <b>if</b> …, …"): the condition is checked when the
 * trigger event occurs and the ability does not trigger at all when it is false. An "unless"
 * clause ("… sacrifice a permanent <b>unless</b> you gained life this turn") is not an
 * intervening-if — that ability always triggers and the condition is only a resolution-time
 * check — so those cards must use {@link #unless}. Both forms re-check the condition on
 * resolution, so only trigger-time behaviour differs.
 */
public record ConditionalEffect(Condition condition, CardEffect wrapped, boolean interveningIf)
        implements CombatDamageTriggerContextEffect {

    /** The common intervening-"if" form (CR 603.4); see {@link #unless} for the other template. */
    public ConditionalEffect(Condition condition, CardEffect wrapped) {
        this(condition, wrapped, true);
    }

    /**
     * The "unless" form: the ability always triggers and {@code condition} gates the effect only
     * as it resolves. {@code condition} still reads "true means apply the wrapped effect", so an
     * "unless you gained life" card passes a {@code DidntGainLifeThisTurn} condition.
     */
    public static ConditionalEffect unless(Condition condition, CardEffect wrapped) {
        return new ConditionalEffect(condition, wrapped, false);
    }

    /** Human-readable condition name for log messages (e.g. "metalcraft", "equipped"). */
    public String conditionName() {
        return condition.conditionName();
    }

    /** Human-readable reason shown when the condition is not met (e.g. "fewer than three artifacts"). */
    public String conditionNotMetReason() {
        return condition.conditionNotMetReason();
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return wrapped instanceof CombatDamageTriggerContextEffect contextEffect
                ? contextEffect.combatDamageTriggerContext()
                : null;
    }
}
