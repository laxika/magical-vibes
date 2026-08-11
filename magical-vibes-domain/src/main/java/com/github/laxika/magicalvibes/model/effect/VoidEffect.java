package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a number, then artifacts and creatures with that mana value are
 * destroyed, and a target player's nonland cards with that mana value are discarded from their
 * hand.
 */
public record VoidEffect() implements BoardWipeEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
