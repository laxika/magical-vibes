package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Resolves several {@link CardEffect} steps in order, as if they were consecutive effects on the
 * same stack entry. The engine ({@code EffectResolutionService}) splices the steps into the entry's
 * live effect list when this effect is reached, so each step resolves through the ordinary loop —
 * pause/resume for async player input and nested wrappers work unchanged.
 *
 * <p><b>Semantics.</b> Steps resolve strictly in order with <em>no data flow between them</em>: there
 * is NO "if you do" contingency between steps. A step that no-ops (its target is gone, its condition
 * is unmet) does not stop the later steps — they still resolve. This is exactly how a flat list of
 * effects on one spell slot behaves, not the gated "if you do the first, then the second" of a
 * {@code MayEffect}-style contingency.</p>
 *
 * <p><b>When to use.</b> Only when a single-effect wrapper must gate several steps as one unit
 * (e.g. {@code MayEffect(SequenceEffect.of(a, b))}, {@code ConditionalEffect(cond, SequenceEffect.of(a, b))},
 * {@code ClashEffect(SequenceEffect.of(a, b))}, {@code FlipCoinWinEffect(SequenceEffect.of(a, b))}), or when
 * several steps must stay a single atomic triggered ability on one trigger slot (trigger collectors push one
 * stack entry per slot effect, so a multi-step bundle on a trigger slot must be one effect). For a plain spell
 * or ability effect list, prefer flat {@code addEffect(...)} calls (the Act of Treason / Drain Life pattern) —
 * do NOT wrap them in a sequence.</p>
 *
 * <p><b>Two dispatch paths.</b> Most wrappers ({@code MayEffect}, {@code ConditionalEffect}, trigger slots)
 * route their child back through {@code EffectResolutionService}'s loop, so the splice above applies and steps
 * may pause/resume for async player input. {@code ClashEffect} and {@code FlipCoinWinEffect} instead dispatch
 * their win/branch reward <em>synchronously</em> (via each step's own handler, not the loop), so a
 * {@code SequenceEffect} used there must contain only synchronous steps (no async player-input pause between
 * steps). Sentry Oak's {@code ClashEffect(SequenceEffect.of(BoostSelfEffect, RemoveKeywordEffect))} is the
 * canonical example — both steps resolve without interaction.</p>
 *
 * <p><b>Targeting.</b> {@link #targetSpec()} returns the first step with an explicitly declared
 * target, so at cast time the entry selects a single target for the sequence exactly as multiple
 * flat targeting effects on one slot share the entry's one target. An implicit source binding such
 * as {@link SacrificeSelfEffect} does not consume a target slot. Multi-target groups inside a sequence
 * are <em>unsupported</em>: the spliced steps are not registered in the card's effect→target-group
 * table, so every targeting step reads the entry's shared {@code targetId}. Use flat, group-bound
 * effects on the card for genuinely multi-target abilities.</p>
 *
 * <p>When used in an end-step trigger, the active end-step player is also carried in the stack
 * entry's {@code targetId}, allowing steps such as {@link DrawCardForTargetPlayerEffect} to act on
 * "that player".</p>
 */
public record SequenceEffect(List<CardEffect> steps, int controllerDrawCount, boolean onlyIfSacrificed)
        implements CombatDamageTriggerContextEffect, CombatDamageDealerAwareEffect, EndStepPlayerTargetedEffect {

    public SequenceEffect(List<CardEffect> steps) {
        this(steps, 0, false);
    }

    public SequenceEffect(List<CardEffect> steps, int controllerDrawCount) {
        this(steps, controllerDrawCount, false);
    }

    public SequenceEffect {
        steps = List.copyOf(steps);
        if (steps.size() < 2) {
            throw new IllegalArgumentException("SequenceEffect requires at least two steps, got " + steps.size());
        }
    }

    /** Convenience factory: {@code SequenceEffect.of(stepA, stepB, ...)}. */
    public static SequenceEffect of(CardEffect... steps) {
        return new SequenceEffect(List.of(steps));
    }

    /**
     * Returns the first explicitly declared target in the sequence. Implicit source bindings such
     * as {@link SacrificeSelfEffect} do not consume a target slot; they are retained only when the
     * sequence has no explicit target at all.
     */
    public static SequenceEffect onSecondControllerDraw(CardEffect... steps) {
        return new SequenceEffect(List.of(steps), 2);
    }

    /** Creates a sequence that triggers only when its source permanent was sacrificed. */
    public static SequenceEffect sacrificeOnly(CardEffect... steps) {
        return new SequenceEffect(List.of(steps), 0, true);
    }

    @Override
    public boolean triggersOnControllerDrawCount(int cardsDrawnThisTurn) {
        return controllerDrawCount == 0 || controllerDrawCount == cardsDrawnThisTurn;
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }
    @Override
    public TargetSpec targetSpec() {
        TargetSpec implicitSourceSpec = TargetSpec.NONE;
        for (CardEffect step : steps) {
            TargetSpec spec = step.targetSpec();
            if (spec.declaredTarget() != null) {
                return spec;
            }
            if (spec.selfTargeting()) {
                implicitSourceSpec = spec;
            }
        }
        return implicitSourceSpec;
    }

    /**
     * On an {@code ON_COMBAT_DAMAGE_TO_PLAYER} slot the sequence resolves as one stack entry, so it
     * must request the richest stack-entry shape any step needs. {@code DAMAGED_PLAYER} binds both
     * the damaged player and the source (a superset of {@code SOURCE_SELF}); a step that only needs
     * the source ("…and sacrifice this creature") still resolves correctly under it. Steps combining
     * {@code DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT} (no source) with a source-bound step are not a real
     * card and left to the coarse priority below.
     */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        TriggerContext result = null;
        for (CardEffect step : steps) {
            if (!(step instanceof CombatDamageTriggerContextEffect contextEffect)) {
                continue;
            }
            TriggerContext stepContext = contextEffect.combatDamageTriggerContext();
            if (stepContext == null) {
                continue;
            }
            if (stepContext == TriggerContext.DAMAGED_PLAYER) {
                return TriggerContext.DAMAGED_PLAYER;
            }
            if (result == null || stepContext == TriggerContext.SOURCE_SELF) {
                result = stepContext;
            }
        }
        return result;
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        return new SequenceEffect(steps.stream()
                .map(step -> step instanceof CombatDamageDealerAwareEffect aware
                        ? aware.withCombatDamageDealerIds(dealerIds)
                        : step)
                .toList(), controllerDrawCount, onlyIfSacrificed);
    }
}
