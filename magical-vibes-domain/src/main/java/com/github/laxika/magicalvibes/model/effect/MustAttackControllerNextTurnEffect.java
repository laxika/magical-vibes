package com.github.laxika.magicalvibes.model.effect;

/**
 * "During target player's next turn, creatures that player controls attack you if able." (Taunt).
 *
 * <p>Targets a player. On resolution it registers a delayed, turn-scoped requirement keyed by the
 * affected player ({@code GameData.tauntedNextTurn}). When that player's next turn begins the turn
 * engine promotes it to {@code GameData.tauntedThisTurn}, and the declare-attackers step then forces
 * every creature the player controls that can attack to do so and to attack the effect's controller
 * (evaluated live so creatures gained during that turn are covered). Per CR 508.1d the affected
 * player is not required to pay any attack costs.
 */
public record MustAttackControllerNextTurnEffect() implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
