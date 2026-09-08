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
        boolean alternateCost,
        boolean spectacle,
        boolean controlledMountAsCast,
        boolean controlledFaerieAsCast,
        boolean collectEvidenceCostPaid,
        boolean castDuringMainPhase,
        int eventValue,
        boolean waterbendCostPaid,
        boolean giftPromised,
        boolean revealCardFromHandCostPaid,
        boolean controlledDragonAsCast
, boolean treasureManaSpentToActivate, boolean teamworkCostPaid) {
    public ConditionContext(
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
        boolean alternateCost,
        boolean spectacle,
        boolean controlledMountAsCast,
        boolean controlledFaerieAsCast,
        boolean collectEvidenceCostPaid,
        boolean castDuringMainPhase,
        int eventValue,
        boolean waterbendCostPaid,
        boolean giftPromised,
        boolean revealCardFromHandCostPaid,
        boolean controlledDragonAsCast
, boolean treasureManaSpentToActivate) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl, madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard, staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId, triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts, alternateCost, spectacle, controlledMountAsCast, controlledFaerieAsCast, collectEvidenceCostPaid, castDuringMainPhase, eventValue, waterbendCostPaid, giftPromised, revealCardFromHandCostPaid, controlledDragonAsCast, treasureManaSpentToActivate, false);
    }

        public ConditionContext(
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
        boolean alternateCost,
        boolean spectacle,
        boolean controlledMountAsCast,
        boolean controlledFaerieAsCast,
        boolean collectEvidenceCostPaid,
        boolean castDuringMainPhase,
        int eventValue,
        boolean waterbendCostPaid,
        boolean giftPromised,
        boolean revealCardFromHandCostPaid,
        boolean controlledDragonAsCast
) {
            this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl, madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard, staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId, triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts, alternateCost, spectacle, controlledMountAsCast, controlledFaerieAsCast, collectEvidenceCostPaid, castDuringMainPhase, eventValue, waterbendCostPaid, giftPromised, revealCardFromHandCostPaid, controlledDragonAsCast, false);
        }

    public ConditionContext {
        repeatedAdditionalCosts = repeatedAdditionalCosts == null
                ? List.of()
                : List.copyOf(repeatedAdditionalCosts);
    }

    public ConditionContext(
            UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent, Card sourceCard,
            boolean kicked, boolean buyback, boolean prowl, boolean madness,
            boolean castForForetell, boolean overloaded, Zone sourceZone, int xValue,
            UUID targetId, Card triggeringCard, boolean staticEvaluation,
            boolean putCounterCostPaid, boolean beholdCostPaid, UUID triggeringPermanentId,
            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
            List<String> repeatedAdditionalCosts, boolean alternateCost, boolean spectacle,
            boolean controlledMountAsCast, boolean collectEvidenceCostPaid,
            boolean castDuringMainPhase, int eventValue, boolean waterbendCostPaid) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts,
                alternateCost, spectacle, controlledMountAsCast, false, collectEvidenceCostPaid,
                castDuringMainPhase, eventValue, waterbendCostPaid, false,
                false, false, false);
    }

    public ConditionContext(
            UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent, Card sourceCard,
            boolean kicked, boolean buyback, boolean prowl, boolean madness,
            boolean castForForetell, boolean overloaded, Zone sourceZone, int xValue,
            UUID targetId, Card triggeringCard, boolean staticEvaluation,
            boolean putCounterCostPaid, boolean beholdCostPaid, UUID triggeringPermanentId,
            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
            List<String> repeatedAdditionalCosts, boolean alternateCost, boolean spectacle,
            boolean controlledMountAsCast, boolean controlledFaerieAsCast,
            boolean castDuringMainPhase, int eventValue) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts,
                alternateCost, spectacle, controlledMountAsCast, controlledFaerieAsCast, false,
                castDuringMainPhase, eventValue, false, false, false, false, false);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean madness, boolean castForForetell, boolean overloaded,
                            Zone sourceZone, int xValue, UUID targetId, Card triggeringCard,
                            boolean staticEvaluation, boolean putCounterCostPaid,
                            boolean beholdCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
                            List<String> repeatedAdditionalCosts, boolean alternateCost,
                            boolean spectacle) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts,
                alternateCost, spectacle, false, false, false, 0, false);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean madness, boolean castForForetell, boolean overloaded,
                            Zone sourceZone, int xValue, UUID targetId, Card triggeringCard,
                            boolean staticEvaluation, boolean putCounterCostPaid,
                            boolean beholdCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
                            List<String> repeatedAdditionalCosts, boolean alternateCost,
                            int eventValue) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts,
                alternateCost, false, false, false, false, eventValue, false);
    }

    public ConditionContext(UUID controllerId, UUID sourcePermanentId, Permanent sourcePermanent,
                            Card sourceCard, boolean kicked, boolean buyback, boolean prowl,
                            boolean madness, boolean castForForetell, boolean overloaded,
                            Zone sourceZone, int xValue, UUID targetId, Card triggeringCard,
                            boolean staticEvaluation, boolean putCounterCostPaid,
                            boolean beholdCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
                            List<String> repeatedAdditionalCosts, boolean alternateCost) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, castForForetell, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, beholdCostPaid, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts,
                alternateCost, false);
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
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts, false, false);
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
                            boolean madness, boolean overloaded, Zone sourceZone, int xValue,
                            UUID targetId, Card triggeringCard, boolean staticEvaluation,
                            boolean putCounterCostPaid, UUID triggeringPermanentId,
                            Integer triggeringPermanentPowerAtTrigger, Card sacrificedCard,
                            List<String> repeatedAdditionalCosts, boolean alternateCost) {
        this(controllerId, sourcePermanentId, sourcePermanent, sourceCard, kicked, buyback, prowl,
                madness, false, overloaded, sourceZone, xValue, targetId, triggeringCard,
                staticEvaluation, putCounterCostPaid, false, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, sacrificedCard, repeatedAdditionalCosts,
                alternateCost, false, false, false, false, 0, false);
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
        UUID sourcePermanentId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getSourcePermanentSnapshot() == null
                        ? null : entry.getSourcePermanentSnapshot().getId();
        Zone sourceZone = entry.getSourceZone() != null
                ? entry.getSourceZone()
                : entry.getSourcePermanentSnapshot() == null
                        ? null : entry.getSourcePermanentSnapshot().getCastFromZone();
        return new ConditionContext(entry.getControllerId(), sourcePermanentId,
                entry.getSourcePermanentSnapshot(), entry.getCard(), entry.isKicked(), entry.isBuyback(),
                entry.isProwl(), entry.isMadness(), entry.isCastForForetell(), entry.isOverloaded(),
                sourceZone, entry.getXValue(), entry.getTargetId(),
                entry.getExiledCostCardSnapshot(), false,
                entry.isPutCounterCostPaid(), entry.isBeholdCostPaid(), entry.getTriggeringPermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(), entry.getSacrificedCard() != null
                        ? entry.getSacrificedCard() : entry.getSacrificedCardSnapshot(),
                entry.getRepeatedAdditionalCosts(), entry.isAlternateCost(), entry.isSpectacle(),
                entry.isControlledMountAsCast(), entry.isControlledFaerieAsCast(),
                entry.isCollectEvidenceCostPaid(),
                entry.isCastDuringMainPhase(), entry.getEventValue(), entry.isWaterbendCostPaid(),
                entry.isGiftPromised(), entry.isRevealCardFromHandCostPaid(),
                entry.isControlledDragonAsCast(), entry.getActivationTreasureManaSpent() > 0 || entry.isActivationUsedTreasureMana(), entry.isTeamworkCostPaid());
    }

    public static ConditionContext forPermanent(Permanent permanent, UUID controllerId) {
        return new ConditionContext(controllerId, permanent.getId(), permanent,
                permanent.getCard(), permanent.isKicked(), false, permanent.isProwl(), permanent.isMadness(), false, false,
                null, 0, null, null, false, false, false, null, null, null,
                permanent.getRepeatedAdditionalCosts(), permanent.isAlternateCost(),
                permanent.isSpectacle(), false, permanent.isCollectEvidenceCostPaid(), false, 0, false);
    }

    public static ConditionContext forStaticEffect(Permanent source, UUID controllerId) {
        return new ConditionContext(controllerId, source.getId(), source,
                source.getCard(), source.isKicked(), false, source.isProwl(), source.isMadness(), false, false,
                null, 0, null, null, true, false, false, null, null, null,
                source.getRepeatedAdditionalCosts(), source.isAlternateCost(),
                source.isSpectacle(), false, source.isCollectEvidenceCostPaid(), false, 0, false);
    }

    public static ConditionContext forCasting(UUID castingPlayerId) {
        return forCasting(castingPlayerId, false, false);
    }

    public static ConditionContext forCasting(UUID castingPlayerId, boolean collectEvidenceCostPaid) {
        return forCasting(castingPlayerId, false, null, collectEvidenceCostPaid);
    }

    public static ConditionContext forCasting(UUID castingPlayerId, boolean kicked,
                                               boolean collectEvidenceCostPaid) {
        return forCasting(castingPlayerId, kicked, null, collectEvidenceCostPaid);
    }

    public static ConditionContext forCasting(UUID castingPlayerId, Zone sourceZone,
                                               boolean collectEvidenceCostPaid) {
        return forCasting(castingPlayerId, false, sourceZone, collectEvidenceCostPaid);
    }

    public static ConditionContext forCasting(UUID castingPlayerId, boolean kicked, Zone sourceZone,
                                               boolean collectEvidenceCostPaid) {
        return new ConditionContext(castingPlayerId, null, null, null,
                kicked, false, false, false, false, false, sourceZone, 0, null, null, false,
                false, false, null, null, null, List.of(), false, false, false,
                collectEvidenceCostPaid, false, 0, false);
    }

    public static ConditionContext forCard(Card card, UUID controllerId) {
        return new ConditionContext(controllerId, null, null, card,
                false, false, false, false, null, 0, null, null, false);
    }

    public ConditionContext withXValue(int newXValue) {
        return copy(newXValue, targetId, triggeringCard, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, eventValue);
    }

    public ConditionContext withTargetId(UUID newTargetId) {
        return copy(xValue, newTargetId, triggeringCard, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, eventValue);
    }

    public ConditionContext withTriggeringCard(Card card) {
        return copy(xValue, targetId, card, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, eventValue);
    }

    public ConditionContext withTriggeringPermanentId(UUID permanentId) {
        return copy(xValue, targetId, triggeringCard, permanentId,
                triggeringPermanentPowerAtTrigger, eventValue);
    }

    public ConditionContext withTriggeringPermanentPowerAtTrigger(Integer power) {
        return copy(xValue, targetId, triggeringCard, triggeringPermanentId, power, eventValue);
    }

    private ConditionContext copy(int copiedXValue, UUID copiedTargetId, Card copiedTriggeringCard,
                                  UUID copiedTriggeringPermanentId, Integer copiedTriggeringPower,
                                  int copiedEventValue) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, madness, castForForetell, overloaded, sourceZone,
                copiedXValue, copiedTargetId, copiedTriggeringCard, staticEvaluation,
                putCounterCostPaid, beholdCostPaid, copiedTriggeringPermanentId,
                copiedTriggeringPower, sacrificedCard, repeatedAdditionalCosts, alternateCost,
                spectacle, controlledMountAsCast, controlledFaerieAsCast, collectEvidenceCostPaid,
                castDuringMainPhase, copiedEventValue, waterbendCostPaid, giftPromised,
                revealCardFromHandCostPaid, controlledDragonAsCast, treasureManaSpentToActivate, teamworkCostPaid);
    }

    public ConditionContext withTeamworkCostPaid(boolean paid) {
        return new ConditionContext(controllerId, sourcePermanentId, sourcePermanent, sourceCard,
                kicked, buyback, prowl, madness, castForForetell, overloaded, sourceZone, xValue,
                targetId, triggeringCard, staticEvaluation, putCounterCostPaid, beholdCostPaid,
                triggeringPermanentId, triggeringPermanentPowerAtTrigger, sacrificedCard,
                repeatedAdditionalCosts, alternateCost, spectacle, controlledMountAsCast,
                controlledFaerieAsCast, collectEvidenceCostPaid, castDuringMainPhase, eventValue,
                waterbendCostPaid, giftPromised, revealCardFromHandCostPaid, controlledDragonAsCast,
                treasureManaSpentToActivate, paid);
    }

    public ConditionContext withEventValue(int newEventValue) {
        return copy(xValue, targetId, triggeringCard, triggeringPermanentId,
                triggeringPermanentPowerAtTrigger, newEventValue);
    }
}
