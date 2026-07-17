package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted player activates a mana ability of each land they control (adding that mana to
 * their own pool), then loses all unspent mana; the spell's controller adds the mana lost this
 * way. Targets a player via a {@code PlayerPredicateTargetFilter}. Used by Drain Power.
 */
public record DrainTargetPlayersLandManaEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
