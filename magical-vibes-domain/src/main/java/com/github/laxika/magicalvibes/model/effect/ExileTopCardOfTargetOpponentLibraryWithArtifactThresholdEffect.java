package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of a targeted opponent's library, creates a Treasure, and gives the
 * controller permission to cast a nonland card from exile this turn. If that card's mana value is
 * less than the controller's artifact count after creating the Treasure, a free-cast choice is
 * offered first.
 */
public record ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
