package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.UUID;

/**
 * Evaluation context for a {@link com.github.laxika.magicalvibes.model.condition.Condition}.
 * Carries source-of-truth values shared by stack, trigger, entry, and static evaluations.
 */
public record ConditionContext(
        UUID controllerId,
        UUID sourcePermanentId,
        Permanent sourcePermanent,
        Card sourceCard,
        boolean kicked,
        boolean buyback,
        boolean prowl,
        boolean madness,
        boolean castForForetell,
        boolean overloaded,
        Zone sourceZone,
        int xValue,
        UUID targetId,
        Card triggeringCard,
        boolean staticEvaluation,
        boolean putCounterCostPaid,
        boolean beholdCostPaid,
        UUID triggeringPermanentId,
        Integer triggeringPermanentPowerAtTrigger,
        Card sacrificedCard,
        List<String> repeatedAdditionalCosts,
        boolean alternateCost
) {
    public ConditionContext {
        repeatedAdditionalCosts = repeatedAdditionalCosts == null
                ? List.of()
                : List.copyOf(repeatedAdditionalCosts);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean madness, boolean castForForetell, boolean overloaded,
                            Zone sourceZone, int xValue, UUID targetId, Card triggeringCard,
                            boolean staticEvaluation, boolean putCounterCostPaid,
                            boolean beholdCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
                            List<String> repeatedAdditionalCosts) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts, false);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean madness, boolean overloaded, Zone sourceZone, int xValue,
                            UUID targetId, Card triggeringCard, boolean staticEvaluation,
                            boolean putCounterCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
                            List<String> repeatedAdditionalCosts) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, false, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, false, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean madness, boolean overloaded, Zone sourceZone, int xValue,
                            UUID targetId, Card triggeringCard, boolean staticEvaluation,
                            boolean putCounterCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, overloaded, sourceZone, xValue, targetId, triggeringCard, staticEvaluation,
                putCounterCostPaid, triggeringPermanentId, triggeringPermanentPowerAtTrigger,
                sacrificedCard, List.of());
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean overloaded, Zone sourceZone, int xValue, UUID targetId,
                            Card triggeringCard, boolean staticEvaluation,
                            boolean putCounterCostPaid, UUID triggeringPermanentId) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                false, false, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, false, triggeringPermanentId,
                null, null, List.of());
    }

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

    public static ConditionContext forStackEntry(StackEntry entry) {
        return new ConditionContext(entry.getControllerId(), entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(), entry.getCard(), entry.isKicked(), entry.isBuyback(),
                entry.isProwl(), entry.isMadness(), entry.isCastForForetell(), entry.isOverloaded(),
                entry.getSourceZone(), entry.getXValue(), entry.getTargetId(), null, false,
                entry.isPutCounterCostPaid(), entry.isBeholdCostPaid(), entry.getTriggeringPermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(), entry.getSacrificedCard(),
                entry.getRepeatedAdditionalCosts(), entry.isAlternateCost());
    }

    public static ConditionContext forPermanent(Permanent permanent, UUID controllerId) {
        return new ConditionContext(controllerId, permanent.getId(), permanent,
                permanent.getCard(), permanent.isKicked(), false, permanent.isProwl(), false, false, false,
                null, 0, null, null, false, false, false, null, null, null,
                permanent.getRepeatedAdditionalCosts());
    }

    public static ConditionContext forStaticEffect(Permanent source, UUID controllerId) {
        return new ConditionContext(controllerId, source.getId(), source,
                source.getCard(), source.isKicked(), false, source.isProwl(), false, false, false,
                null, 0, null, null, true, false, false, null, null, null,
                source.getRepeatedAdditionalCosts());
    }

    public static ConditionContext forCasting(UUID castingPlayerId) {
        return new ConditionContext(castingPlayerId, null, null, null,
                false, false, false, false, null, 0, null, null, false);
    }

    public static ConditionContext forCard(Card card, UUID controllerId) {
        return new ConditionContext(controllerId, null, null, card,
                false, false, false, false, null, 0, null, null, false);
    }

    public ConditionContext withXValue(int newXValue) {
        return copy(newXValue, targetId, triggeringCard, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger);
    }

    public ConditionContext withTargetId(UUID newTargetId) {
        return copy(xValue, newTargetId, triggeringCard, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger);
    }

    public ConditionContext withTriggeringCard(Card card) {
        return copy(xValue, targetId, card, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger);
    }

    public ConditionContext withTriggeringPermanentId(UUID permanentId) {
        return copy(xValue, targetId, triggeringCard, permanentId,
                triggeringPermanentPowerAtTrigger);
    }

    public ConditionContext withTriggeringPermanentPowerAtTrigger(Integer power) {
        return copy(xValue, targetId, triggeringCard, triggeringPermanentId, power);
    }

    private ConditionContext copy(int copiedXValue, UUID copiedTargetId, Card copiedTriggeringCard,
                                  UUID copiedTriggeringPermanentId, Integer copiedTriggeringPower) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, madness, castForForetell, overloaded, sourceZone,
                copiedXValue, copiedTargetId, copiedTriggeringCard, staticEvaluation,
                putCounterCostPaid, beholdCostPaid, copiedTriggeringPermanentId,
                copiedTriggeringPower, sacrificedCard, repeatedAdditionalCosts, alternateCost);
    }
}
