package com.github.laxika.magicalvibes.model.effect;

/** Conjures a duplicate of a random card from the target player's library into the controller's hand. */
public record ConjureRandomCardFromTargetPlayerLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
