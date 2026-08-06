package com.github.laxika.magicalvibes.model.effect;

/**
 * "During target player's next turn, creatures that player controls attack {something} if able."
 *
 * <p>Targets a player. On resolution it registers a delayed, turn-scoped requirement keyed by the
 * affected player ({@code GameData.tauntedNextTurn}), storing the id of the object those creatures must
 * attack. When that player's next turn begins the turn engine promotes it to
 * {@code GameData.tauntedThisTurn}, and the declare-attackers step then forces every creature the player
 * controls that can attack to do so and to attack the stored object (evaluated live, so creatures gained
 * during that turn are covered). Per CR 508.1d the affected player is not required to pay any attack
 * costs.
 *
 * <p>For {@link TauntTarget#SOURCE_PERMANENT} the combat services compare the stored id against
 * {@code AttackLegalityService.getValidAttackTargetIds}, which already contains the defending player's
 * planeswalker permanent ids, so the requirement lapses on its own if the source leaves the battlefield
 * before that turn.
 *
 * @param tauntTarget whether the forced attack lands on the ability's controller (Taunt) or on its own
 *                    source permanent (Gideon Jura)
 */
public record MustAttackNextTurnEffect(TauntTarget tauntTarget) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
