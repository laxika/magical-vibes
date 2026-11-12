package com.github.laxika.magicalvibes.model.effect;

/**
 * "Select target player at random. That player discards a card. If that player discards a land card this way, they
 * lose 3 life." The player is a target picked at random as the ability is put on the stack (CR 603.3d). Used by
 * Furnace Layer's planeswalk-to and upkeep triggers.
 */
public record RandomPlayerDiscardsAndLosesLifeIfLandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public boolean targetChosenAtRandom() {
        return true;
    }
}
