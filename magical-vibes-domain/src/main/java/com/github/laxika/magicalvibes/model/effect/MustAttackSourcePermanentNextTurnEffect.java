package com.github.laxika.magicalvibes.model.effect;

/**
 * "During target opponent's next turn, creatures that player controls attack this permanent if able."
 * (Gideon Jura's +2).
 *
 * <p>Targets a player. The planeswalker-directed sibling of {@link MustAttackControllerNextTurnEffect}:
 * both register the same delayed, turn-scoped requirement in {@code GameData.tauntedNextTurn}, but the
 * forced attack target stored here is the ability's source permanent rather than the ability's
 * controller. The combat services compare that stored id against
 * {@code AttackLegalityService.getValidAttackTargetIds}, which already contains the defending player's
 * planeswalker permanent ids, so the requirement lapses on its own if the source leaves the
 * battlefield before that turn. Per CR 508.1d the affected player is not required to pay attack costs.
 */
public record MustAttackSourcePermanentNextTurnEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
