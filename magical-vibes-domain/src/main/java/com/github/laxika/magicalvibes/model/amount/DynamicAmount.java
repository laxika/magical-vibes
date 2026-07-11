package com.github.laxika.magicalvibes.model.amount;

/**
 * A dynamically computed quantity attached to an effect (the numeric sibling of
 * {@link com.github.laxika.magicalvibes.model.condition.Condition}). Amounts are pure
 * data — evaluation lives in the engine's {@code AmountEvaluationService}, which
 * switches exhaustively over this sealed hierarchy so that a missing amount is a
 * compile error rather than a silent runtime fallback.
 *
 * <p>Effects that only differ in how a number is derived ("+1/+1 for each artifact you
 * control", "+X/+0 where X is the mana spent", …) should be a single effect record
 * parameterized with a {@code DynamicAmount} instead of one record per derivation.
 */
public sealed interface DynamicAmount permits
        AttachmentsOnSource,
        CardsInGraveyard,
        CardsInHand,
        ControllerLifeTotal,
        CountersOnLinkedPermanent,
        CountersOnSource,
        CreatureDeathsThisTurn,
        CreaturesBlockingSource,
        DamageDealtToTargetPlayerThisTurn,
        Divided,
        EventValue,
        Fixed,
        FixedIfControlledCreaturesTotalToughnessAtLeast,
        FixedIfControlsAllNamed,
        GreatestPowerAmongControlled,
        ImprintedCreaturePower,
        ImprintedCreatureToughness,
        LandsMatchingImprintedName,
        ManaSpentToCast,
        OpponentPoisonCounters,
        PermanentCount,
        Scaled,
        SourcePower,
        SourceToughness,
        Sum,
        TargetToughness,
        TargetPower,
        XValue {
}
