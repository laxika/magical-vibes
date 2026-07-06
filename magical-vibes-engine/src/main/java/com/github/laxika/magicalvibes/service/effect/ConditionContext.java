package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

/**
 * Evaluation context for a {@link com.github.laxika.magicalvibes.model.condition.Condition}.
 * Carries the source-of-truth values that differ between evaluation sites (stack resolution,
 * trigger collection, ETB gating, static bonus computation) so that
 * {@link ConditionEvaluationService} can host a single exhaustive switch.
 *
 * @param controllerId     controller of the spell/ability/permanent the condition belongs to
 * @param sourcePermanentId id of the source permanent, when one exists
 * @param sourcePermanent  the source permanent itself, when the call site has it at hand
 * @param sourceCard       the source card (stack entry card, entering card, or permanent's card)
 * @param kicked           whether the spell/permanent was kicked (from the stack entry or permanent)
 * @param sourceZone       zone the spell was cast from, when known
 * @param xValue           snapshotted numeric context (attacker count, mana spent)
 * @param targetId         current target id, when resolving a targeted effect
 * @param triggeringCard   the entering/triggering card for enter-trigger conditions
 * @param staticEvaluation when {@code true}, permanent predicates are matched with the
 *                         recursion-safe static filter matcher instead of the general one
 */
public record ConditionContext(
        UUID controllerId,
        UUID sourcePermanentId,
        Permanent sourcePermanent,
        Card sourceCard,
        boolean kicked,
        Zone sourceZone,
        int xValue,
        UUID targetId,
        Card triggeringCard,
        boolean staticEvaluation
) {

    /** Context for resolving an effect on a stack entry (stack resolution time). */
    public static ConditionContext forStackEntry(StackEntry entry) {
        return new ConditionContext(entry.getControllerId(), entry.getSourcePermanentId(), null,
                entry.getCard(), entry.isKicked(), entry.getSourceZone(), entry.getXValue(),
                entry.getTargetId(), null, false);
    }

    /** Context for trigger-time (intervening-if) checks against a battlefield permanent. */
    public static ConditionContext forPermanent(Permanent permanent, UUID controllerId) {
        return new ConditionContext(controllerId, permanent.getId(), permanent,
                permanent.getCard(), permanent.isKicked(), null, 0, null, null, false);
    }

    /** Context for static (continuous) effect computation from a source permanent. */
    public static ConditionContext forStaticEffect(Permanent source, UUID controllerId) {
        return new ConditionContext(controllerId, source.getId(), source,
                source.getCard(), source.isKicked(), null, 0, null, null, true);
    }

    /**
     * Context for gating a spell's cast cost on a condition, before any stack entry exists.
     * A spell being cast from hand has no source permanent; only controller-relative conditions
     * (metalcraft, controls-a-permanent, opponent creature counts) are meaningful here.
     */
    public static ConditionContext forCasting(UUID castingPlayerId) {
        return new ConditionContext(castingPlayerId, null, null, null, false, null, 0, null, null, false);
    }

    /** Returns a copy with the given snapshotted numeric value (attacker count, mana spent). */
    public ConditionContext withXValue(int newXValue) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, sourceZone, newXValue, targetId, triggeringCard, staticEvaluation);
    }

    /** Returns a copy with the given triggering (entering) card. */
    public ConditionContext withTriggeringCard(Card card) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, sourceZone, xValue, targetId, card, staticEvaluation);
    }
}
