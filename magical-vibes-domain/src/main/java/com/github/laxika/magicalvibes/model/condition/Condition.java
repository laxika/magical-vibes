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
        AnyPlayerControlsPermanentCount,
        AttacksAlone,
        BlockedByMinCreatures,
        CardsInLibraryAtLeast,
        CardsInHandAtLeast,
        CardsLeftGraveyardThisTurn,
        CastFromZone,
        CastNotFromHand,
        ControllerCastAnotherSpellThisTurn,
        ControllerLifeAtLeast,
        ControllerLifeAtMost,
        ControllerTurn,
        ControlsAnotherPermanent,
        ControlsPermanent,
        ControlsPermanentCount,
        ControlsPermanentCountAtMost,
        CreatureDiedUnderYourControlThisTurn,
        DefendingPlayerControlsPermanent,
        DefendingPlayerPoisoned,
        DidntAttack,
        DidntGainLifeThisTurn,
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
        NthAbilityResolutionThisTurn,
        OpponentControlsMoreCreatures,
        OpponentControlsMoreLands,
        OpponentControlsPermanent,
        OpponentDealtDamageThisTurn,
        OpponentPoisoned,
        PermanentEnteredThisTurn,
        Raid,
        SelfHasKeyword,
        SourceCounterThreshold,
        SourceHasSubtype,
        SpellManaSpentAtLeast,
        TargetPermanentMatches,
        TopCardOfLibraryColor,
        TwoOrMoreSpellsCastLastTurn,
        WonClash {

    /** Human-readable condition name for log messages (e.g. "metalcraft", "equipped"). */
    String conditionName();

    /** Human-readable reason shown when the condition is not met (e.g. "fewer than three artifacts"). */
    String conditionNotMetReason();
}
