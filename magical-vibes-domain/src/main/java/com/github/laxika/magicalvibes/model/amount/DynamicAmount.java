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
        CountersOnSource,
        CreatureDeathsThisTurn,
        CreaturesBlockingSource,
        Divided,
        Fixed,
        GreatestPowerAmongControlled,
        ImprintedCreaturePower,
        ImprintedCreatureToughness,
        OpponentPoisonCounters,
        PermanentCount,
        Scaled,
        Sum,
        XValue {
}
