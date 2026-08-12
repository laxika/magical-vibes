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
 * @param buyback          whether the spell's buyback cost was paid (from the stack entry)
 * @param prowl            whether the spell/permanent was cast for its prowl cost (from the stack entry or permanent)
 * @param overloaded       whether the spell was cast for its overload cost (CR 702.96a)
 * @param sourceZone       zone the spell was cast from, when known
 * @param xValue           snapshotted numeric context (attacker count, mana spent)
 * @param targetId         current target id, when resolving a targeted effect
 * @param triggeringCard   the entering/triggering card for enter-trigger conditions
 * @param staticEvaluation whether this evaluation is for a static continuous effect
 * @param putCounterCostPaid whether the spell's put-counter additional cost was paid
 * @param triggeringPermanentId the permanent that caused an enter-the-battlefield trigger
 */
public record ConditionContext(
        UUID controllerId,
        UUID sourcePermanentId,
        Permanent sourcePermanent,
        Card sourceCard,
        boolean kicked,
        boolean buyback,
        boolean prowl,
        boolean overloaded,
        Zone sourceZone,
        int xValue,
        UUID targetId,
        Card triggeringCard,
        boolean staticEvaluation,
        boolean putCounterCostPaid,
        UUID triggeringPermanentId
) {

    /** Compatibility constructor for evaluation sites with no cast-cost payment state. */
    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean overloaded, Zone sourceZone, int xValue, UUID targetId,
                            Card triggeringCard, boolean staticEvaluation) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                overloaded, sourceZone, xValue, targetId, triggeringCard, staticEvaluation,
                false, null);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean overloaded, Zone sourceZone, int xValue, UUID targetId,
                            Card triggeringCard, boolean staticEvaluation,
                            boolean putCounterCostPaid) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                overloaded, sourceZone, xValue, targetId, triggeringCard, staticEvaluation,
                putCounterCostPaid, null);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean overloaded, Zone sourceZone, int xValue, UUID targetId,
                            Card triggeringCard, boolean staticEvaluation,
                            UUID triggeringPermanentId) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                overloaded, sourceZone, xValue, targetId, triggeringCard, staticEvaluation,
                false, triggeringPermanentId);
    }

    /** Context for resolving an effect on a stack entry (stack resolution time). */
    public static ConditionContext forStackEntry(StackEntry entry) {
        return new ConditionContext(entry.getControllerId(), entry.getSourcePermanentId(), null,
                entry.getCard(), entry.isKicked(), entry.isBuyback(), entry.isProwl(), entry.isOverloaded(),
                entry.getSourceZone(), entry.getXValue(), entry.getTargetId(), null, false,
                entry.isPutCounterCostPaid(), entry.getTriggeringPermanentId());
    }

    /** Context for trigger-time (intervening-if) checks against a battlefield permanent. */
    public static ConditionContext forPermanent(Permanent permanent, UUID controllerId) {
        return new ConditionContext(controllerId, permanent.getId(), permanent,
                permanent.getCard(), permanent.isKicked(), false, permanent.isProwl(), false, null, 0, null, null,
                false);
    }

    /**
     * Context for static (continuous) effect computation from a source permanent. Identical to
     * {@link #forPermanent} — a static-effect call site no longer selects the recursion-safe
     * matchers, {@code GameQueryService.isStaticEvaluationActive()} observes them — and kept
     * only because it says at the call site which kind of evaluation is being set up.
     */
    public static ConditionContext forStaticEffect(Permanent source, UUID controllerId) {
        return new ConditionContext(controllerId, source.getId(), source,
                source.getCard(), source.isKicked(), false, source.isProwl(), false, null, 0, null, null, true);
    }

    /**
     * Context for gating a spell's cast cost on a condition, before any stack entry exists.
     * A spell being cast from hand has no source permanent; only controller-relative conditions
     * (metalcraft, controls-a-permanent, opponent creature counts) are meaningful here.
     */
    public static ConditionContext forCasting(UUID castingPlayerId) {
        return new ConditionContext(castingPlayerId, null, null, null, false, false, false, false, null, 0, null,
                null, false);
    }

    /**
     * Context for a card that is not currently a battlefield permanent (e.g. graveyard activated
     * ability gates such as {@code CardsAboveSelfInGraveyard}).
     */
    public static ConditionContext forCard(Card card, UUID controllerId) {
        return new ConditionContext(controllerId, null, null, card, false, false, false, false, null, 0, null, null,
                false);
    }

    /** Returns a copy with the given snapshotted numeric value (attacker count, mana spent). */
    public ConditionContext withXValue(int newXValue) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, overloaded, sourceZone, newXValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, triggeringPermanentId);
    }

    /** Returns a copy with the given target id (e.g. a multi-target group's chosen target). */
    public ConditionContext withTargetId(UUID newTargetId) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, overloaded, sourceZone, xValue, newTargetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, triggeringPermanentId);
    }

    /** Returns a copy with the given triggering (entering) card. */
    public ConditionContext withTriggeringCard(Card card) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, overloaded, sourceZone, xValue, targetId, card, staticEvaluation,
                putCounterCostPaid, triggeringPermanentId);
    }

    /** Returns a copy carrying the permanent that caused an enter-the-battlefield trigger. */
    public ConditionContext withTriggeringPermanentId(UUID permanentId) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, permanentId);
    }
}
