package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;

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
 */
public record AmountContext(
        UUID controllerId,
        Permanent sourcePermanent,
        UUID targetPermanentId,
        int xValue,
        int eventValue,
        boolean staticEvaluation
) {

    /** Context for resolving an effect on a stack entry (stack resolution time). */
    public static AmountContext forStackEntry(StackEntry entry, Permanent sourcePermanent) {
        return new AmountContext(entry.getControllerId(), sourcePermanent, entry.getTargetId(),
                entry.getXValue(), entry.getEventValue(), false);
    }

    /** Context for static (continuous) effect computation from a source permanent. */
    public static AmountContext forStaticEffect(Permanent source, UUID controllerId) {
        return new AmountContext(controllerId, source, null, 0, 0, true);
    }

    /**
     * Context for a mana ability resolving outside the stack (no priority round, no {@code xValue}).
     * Mana abilities are resolved immediately from the source permanent and its controller
     * (CR 605.3a), so there is no {@link StackEntry} to read.
     */
    public static AmountContext forManaAbility(Permanent source, UUID controllerId) {
        return new AmountContext(controllerId, source, null, 0, 0, false);
    }

    /** Source-less context for heuristic estimation (AI evaluation). */
    public static AmountContext forEstimation(UUID controllerId) {
        return new AmountContext(controllerId, null, null, 0, 0, false);
    }

    /**
     * Context for computing a spell's cast cost, before any stack entry exists. A spell being
     * cast from hand has no source permanent; only player-relative counting amounts
     * (graveyard/battlefield counts) are meaningful here.
     */
    public static AmountContext forCasting(UUID castingPlayerId) {
        return new AmountContext(castingPlayerId, null, null, 0, 0, false);
    }
}
