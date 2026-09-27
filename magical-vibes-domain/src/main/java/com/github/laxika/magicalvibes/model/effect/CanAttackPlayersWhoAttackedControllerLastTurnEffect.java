package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission for the carrier to attack a player who attacked the carrier's controller
 * during that player's last turn, as though the carrier did not have defender.
 */
public record CanAttackPlayersWhoAttackedControllerLastTurnEffect()
        implements NoDefenderAttackPermissionEffect {

    @Override
    public boolean grantsCarrierAttackAsThoughNoDefenderAgainstDefenderWhoAttackedControllerLastTurn() {
        return true;
    }
}
