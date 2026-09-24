package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the creature in one target group attack the player in another target group this turn if
 * able.
 */
public record TargetCreatureMustAttackTargetPlayerThisTurnEffect(
        int creatureTargetGroup, int playerTargetGroup) implements CardEffect {

    /** Uses the first target group for the creature and the second for the player. */
    public TargetCreatureMustAttackTargetPlayerThisTurnEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
