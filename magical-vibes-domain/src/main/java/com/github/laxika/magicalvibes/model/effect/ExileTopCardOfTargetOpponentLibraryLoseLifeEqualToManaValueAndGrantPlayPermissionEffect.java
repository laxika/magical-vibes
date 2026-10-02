package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of a target opponent's library face down, makes the controller lose life
 * equal to its mana value, and grants the controller indefinite normal-cost play permission with
 * mana of any type usable for spells.
 */
public record ExileTopCardOfTargetOpponentLibraryLoseLifeEqualToManaValueAndGrantPlayPermissionEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
