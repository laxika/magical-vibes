package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller names a card, then the target player reveals up to X cards chosen at random from
 * their hand. Every revealed card with the chosen name is discarded. The X value comes from the
 * resolving stack entry.
 */
public record ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
