package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals a target player's library until a creature card is found, then makes the source
 * permanent a copy of that card until end of turn. All cards revealed this way are put on the
 * bottom of that library in a random order.
 */
public record RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
