package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the creatures controlled by two target players attack the other chosen player each
 * combat if able until the controller's next turn.
 */
public record TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect(int targetGroup)
        implements CardEffect {

    public TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect() {
        this(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
