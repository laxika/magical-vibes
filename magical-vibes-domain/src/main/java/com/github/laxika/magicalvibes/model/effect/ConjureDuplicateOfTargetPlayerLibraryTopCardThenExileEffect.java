package com.github.laxika.magicalvibes.model.effect;

/** Conjures a duplicate of the target player's library top card, then exiles that card face down. */
public record ConjureDuplicateOfTargetPlayerLibraryTopCardThenExileEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
