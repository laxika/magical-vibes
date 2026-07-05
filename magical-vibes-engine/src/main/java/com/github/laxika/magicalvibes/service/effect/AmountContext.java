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
 * @param controllerId     controller of the spell/ability/permanent the amount belongs to
 * @param sourcePermanent  the source permanent, when one exists (source-relative amounts
 *                         such as attachments or blockers evaluate to 0 without it)
 * @param xValue           snapshotted numeric context from the stack entry (mana spent, X paid)
 * @param staticEvaluation when {@code true}, permanent predicates are matched with the
 *                         recursion-safe intrinsic matcher (no {@code computeStaticBonus}
 *                         lookups) so static bonus computation cannot recurse
 */
public record AmountContext(
        UUID controllerId,
        Permanent sourcePermanent,
        int xValue,
        boolean staticEvaluation
) {

    /** Context for resolving an effect on a stack entry (stack resolution time). */
    public static AmountContext forStackEntry(StackEntry entry, Permanent sourcePermanent) {
        return new AmountContext(entry.getControllerId(), sourcePermanent, entry.getXValue(), false);
    }

    /** Context for static (continuous) effect computation from a source permanent. */
    public static AmountContext forStaticEffect(Permanent source, UUID controllerId) {
        return new AmountContext(controllerId, source, 0, true);
    }

    /** Source-less context for heuristic estimation (AI evaluation). */
    public static AmountContext forEstimation(UUID controllerId) {
        return new AmountContext(controllerId, null, 0, false);
    }
}
