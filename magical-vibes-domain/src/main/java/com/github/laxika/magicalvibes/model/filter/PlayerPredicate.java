package com.github.laxika.magicalvibes.model.filter;

/**
 * A predicate over a player. Predicates are pure data — evaluation lives in the engine's
 * targeting services, which dispatch over this sealed hierarchy.
 */
public sealed interface PlayerPredicate permits
        PlayerDamagedBySourceCombatThisTurnPredicate,
        PlayerAttackedThisTurnPredicate,
        PlayerDamagedBySourceThisTurnPredicate,
        PlayerDealtDamageThisTurnPredicate,
        PlayerControlsMoreCreaturesThanControllerPredicate,
        PlayerControlsMoreLandsThanControllerPredicate,
        PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate,
        PlayerHasMoreCardsInHandThanControllerPredicate,
        PlayerHasMoreLifeThanControllerPredicate,
        PlayerLostLifeThisTurnPredicate,
        PlayerRelationPredicate {
}
