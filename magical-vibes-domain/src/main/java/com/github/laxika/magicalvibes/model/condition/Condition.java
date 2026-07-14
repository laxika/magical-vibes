package com.github.laxika.magicalvibes.model.condition;

/**
 * A game-state condition attached to a conditional effect wrapper. Conditions are pure
 * data — evaluation lives in the engine's {@code ConditionEvaluationService}, which
 * switches exhaustively over this sealed hierarchy so that a missing condition is a
 * compile error rather than a silent runtime fallback.
 */
public sealed interface Condition permits
        ActivePlayerHandEmpty,
        ActivationCount,
        AnOpponentHandEmpty,
        AnyLibraryAtMost,
        AnyPlayerControlsPermanent,
        AnyPlayerControlsPermanentCount,
        AttackedWithCreaturesThisTurn,
        AttacksAlone,
        BlockedByMinCreatures,
        CardsInLibraryAtLeast,
        CardsInHandAtLeast,
        CardsLeftGraveyardThisTurn,
        CastForProwlCost,
        CastFromZone,
        CastNotFromHand,
        ColorSpentToCast,
        ControllerCastAnotherSpellThisTurn,
        ControllerHasMoreLifeThanAnOpponent,
        ControllerHandEmpty,
        ControllerLifeAtLeast,
        ControllerLifeAtMost,
        ControllerTurn,
        ControlsAnotherPermanent,
        ControlsPermanent,
        ControlsPermanentCount,
        ControlsPermanentCountAtMost,
        ControlledCreaturesTotalPowerAtLeast,
        CreatureDiedUnderYourControlThisTurn,
        DefendingPlayerControlsPermanent,
        DefendingPlayerPoisoned,
        DidntAttack,
        DidntGainLifeThisTurn,
        Enchanted,
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
        NoPlayerHasCardsInHand,
        NoSpellsCastLastTurn,
        NotCondition,
        NotControllerTurn,
        NotKicked,
        NthAbilityResolutionThisTurn,
        OpponentControlsMoreCreatures,
        OpponentControlsMoreLands,
        OpponentControlsPermanent,
        OpponentDealtDamageThisTurn,
        OpponentLostLifeThisTurn,
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

    /**
     * True when, used as an ETB intervening-"if" (CR 603.4), this condition is checked against
     * game state as the trigger would go on the stack (e.g. Metalcraft, Morbid, Raid) rather
     * than being a casting choice already known at cast time (e.g. Kicked). A gate-conditional
     * ETB never creates a cast-time target requirement — its target is chosen only when the
     * trigger is put on the stack (CR 603.3d), and only if the gate is met at that point.
     */
    default boolean isEtbTriggerGate() {
        return false;
    }
}
