package com.github.laxika.magicalvibes.model.effect;

/**
 * Thran Tome's activated ability: reveal the top three cards of your library, have a target
 * opponent choose one, and put the chosen card into your graveyard.
 */
public record ThranTomeRevealTopThreeOpponentChoosesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
