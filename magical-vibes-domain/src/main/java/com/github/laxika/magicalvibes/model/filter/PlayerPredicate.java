package com.github.laxika.magicalvibes.model.filter;

/**
 * A predicate over a player. Predicates are pure data — evaluation lives in the engine's
 * targeting services, which dispatch over this sealed hierarchy.
 */
public sealed interface PlayerPredicate permits
        OpponentPreviouslyDamagedBySourcePredicate,
        PlayerDamagedBySourceCombatThisTurnPredicate,
        PlayerAttackedThisTurnPredicate,
        PlayerDamagedBySourceThisTurnPredicate,
        PlayerDealtDamageThisTurnPredicate,
        PlayerCastSorceryThisTurnPredicate,
        PlayerControlsMoreCreaturesThanControllerPredicate,
        PlayerControlsMoreLandsThanControllerPredicate,
        PlayerControlsMostCreaturesPredicate,
        PlayerControlsMostLandsPredicate,
        PlayerControlsPermanentPredicate,
        PlayerAttackedBySourceThisTurnPredicate,
        PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate,
        PlayerHasMoreCardsInHandThanControllerPredicate,
        PlayerHasMoreLifeThanControllerPredicate,
        PlayerHasMostLifePredicate,
        PlayerLostLifeThisTurnPredicate,
        PlayerTappedLandForManaThisTurnPredicate,
        PlayerHasNoCardsInHandPredicate,
        PlayerHasPoisonCountersAtLeastPredicate,
        PlayerRelationPredicate,
        PlayerIdPredicate,
        PlayerOtherThanTriggeringPlayerPredicate,
        PlayerChosenBySourcePredicate,
        PlayerOtherThanSourceOwnerPredicate,
        PlayerIsActiveOpponentPredicate,
        PlayerCanSearchTheirLibraryPredicate,
        PlayerWithoutMaxSpeedPredicate {
}
