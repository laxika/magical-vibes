package com.github.laxika.magicalvibes.model.condition;

/**
 * A game-state condition attached to a conditional effect wrapper. Conditions are pure
 * data — evaluation lives in the engine's {@code ConditionEvaluationService}, which
 * switches exhaustively over this sealed hierarchy so that a missing condition is a
 * compile error rather than a silent runtime fallback.
 */
public sealed interface Condition permits
        ActivationCount,
        AnyPlayerControlsPermanent,
        AttacksAlone,
        BlockedByMinCreatures,
        CastFromZone,
        CastNotFromHand,
        ControllerCastAnotherSpellThisTurn,
        ControllerLifeAtLeast,
        ControllerLifeAtMost,
        ControllerTurn,
        ControlsAnotherPermanent,
        ControlsPermanent,
        ControlsPermanentCount,
        DefendingPlayerPoisoned,
        DidntAttack,
        Equipped,
        GainedLifeThisTurn,
        GraveyardCardThreshold,
        HasAttacker,
        ImprintedCardNameMatchesEnteringPermanent,
        Kicked,
        Metalcraft,
        MinimumAttackers,
        Morbid,
        NoOtherPermanent,
        NoSpellsCastLastTurn,
        NotControllerTurn,
        NotKicked,
        OpponentControlsPermanent,
        OpponentPoisoned,
        PermanentEnteredThisTurn,
        Raid,
        SelfHasKeyword,
        SourceCounterThreshold,
        SourceHasSubtype,
        SpellManaSpentAtLeast,
        TargetPermanentMatches,
        TopCardOfLibraryColor,
        TwoOrMoreSpellsCastLastTurn {

    /** Human-readable condition name for log messages (e.g. "metalcraft", "equipped"). */
    String conditionName();

    /** Human-readable reason shown when the condition is not met (e.g. "fewer than three artifacts"). */
    String conditionNotMetReason();
}
