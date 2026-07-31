package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.List;
import java.util.UUID;

/**
 * Evaluation context for a {@link com.github.laxika.magicalvibes.model.amount.DynamicAmount}
 * (the numeric sibling of {@link ConditionContext}). Carries the source-of-truth values that
 * differ between evaluation sites so that {@link AmountEvaluationService} can host a single
 * exhaustive switch.
 *
 * @param controllerId      controller of the spell/ability/permanent the amount belongs to
 * @param sourcePermanent   the source permanent, when one exists (source-relative amounts
 *                          such as attachments or blockers evaluate to 0 without it)
 * @param targetPermanentId id of the resolved target permanent, when resolving a targeted effect —
 *                          read from the stack entry the same way {@code ConditionContext.targetId}
 *                          is; target-relative amounts ({@code TargetToughness}) look it up here and
 *                          evaluate to 0 without a legal target
 * @param xValue            snapshotted cast-time context from the stack entry (mana spent, X paid)
 * @param eventValue        snapshotted trigger-event / prior-resolution context from the stack entry
 *                          (life gained, damage dealt, excess damage); read by {@code EventValue}
 * @param staticEvaluation  when {@code true}, permanent predicates are matched with the
 *                          recursion-safe intrinsic matcher (no {@code computeStaticBonus}
 *                          lookups) so static bonus computation cannot recurse
 * @param chosenPermanentId id of the permanent chosen while activating the ability behind this
 *                          entry (e.g. the creature tapped for a {@code TapCreatureCost}); read by
 *                          {@code ChosenPermanentPower}. {@code null} outside stack resolution
 * @param repeatedAdditionalCosts the mana payments chosen for a
 *                          {@code RepeatableAdditionalManaCost} as the spell was cast, one entry
 *                          per repetition; read by {@code RepeatedAdditionalCostCount}. Empty
 *                          outside stack resolution
 */
public record AmountContext(
        UUID controllerId,
        Permanent sourcePermanent,
        UUID targetPermanentId,
        int xValue,
        int eventValue,
        boolean staticEvaluation,
        UUID chosenPermanentId,
        List<String> repeatedAdditionalCosts
) {

    /** Convenience for the common case with no repeatable additional cost payments. */
    public AmountContext(UUID controllerId, Permanent sourcePermanent, UUID targetPermanentId,
                         int xValue, int eventValue, boolean staticEvaluation, UUID chosenPermanentId) {
        this(controllerId, sourcePermanent, targetPermanentId, xValue, eventValue, staticEvaluation,
                chosenPermanentId, List.of());
    }

    /** Convenience for the common case with no chosen permanent (all non-stack-resolution sites). */
    public AmountContext(UUID controllerId, Permanent sourcePermanent, UUID targetPermanentId,
                         int xValue, int eventValue, boolean staticEvaluation) {
        this(controllerId, sourcePermanent, targetPermanentId, xValue, eventValue, staticEvaluation, null);
    }

    /** Context for resolving an effect on a stack entry (stack resolution time). */
    public static AmountContext forStackEntry(StackEntry entry, Permanent sourcePermanent) {
        return new AmountContext(entry.getControllerId(), sourcePermanent, entry.getTargetId(),
                entry.getXValue(), entry.getEventValue(), false, entry.getChosenPermanentId(),
                entry.getRepeatedAdditionalCosts());
    }

    /** Context for static (continuous) effect computation from a source permanent. */
    public static AmountContext forStaticEffect(Permanent source, UUID controllerId) {
        return new AmountContext(controllerId, source, null, 0, 0, true, null);
    }

    /**
     * Context for a mana ability resolving outside the stack (no priority round, no {@code xValue}).
     * Mana abilities are resolved immediately from the source permanent and its controller
     * (CR 605.3a), so there is no {@link StackEntry} to read.
     */
    public static AmountContext forManaAbility(Permanent source, UUID controllerId) {
        return forManaAbility(source, controllerId, 0);
    }

    /**
     * Mana-ability context that carries the activation-time {@code xValue}, for abilities whose
     * cost snapshots a number the mana amount scales off (Soldevi Adnate's sacrificed mana value).
     */
    public static AmountContext forManaAbility(Permanent source, UUID controllerId, int xValue) {
        return new AmountContext(controllerId, source, null, xValue, 0, false, null);
    }

    /** Source-less context for heuristic estimation (AI evaluation). */
    public static AmountContext forEstimation(UUID controllerId) {
        return new AmountContext(controllerId, null, null, 0, 0, false, null);
    }

    /**
     * Context for computing a spell's cast cost, before any stack entry exists. A spell being
     * cast from hand has no source permanent; only player-relative counting amounts
     * (graveyard/battlefield counts) are meaningful here.
     */
    public static AmountContext forCasting(UUID castingPlayerId) {
        return forCasting(castingPlayerId, 0);
    }

    /**
     * Cast-time context that also carries the announced X, for amounts that depend on it before a
     * stack entry exists (Meteor Shower — "X plus 1 damage divided as you choose").
     */
    public static AmountContext forCasting(UUID castingPlayerId, int xValue) {
        return new AmountContext(castingPlayerId, null, null, xValue, 0, false, null);
    }
}
