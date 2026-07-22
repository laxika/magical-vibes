package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Madness (CR 702.34): if you discard this card, discard it into exile instead of the graveyard.
 * When you do, a triggered ability lets you cast it for its madness cost or put it into your
 * graveyard. Casting ignores type-based timing (like Miracle).
 *
 * <p>The cost is declared on the card ({@code addCastingOption(new MadnessCast("{1}{B}"))});
 * Scryfall only supplies the {@link Keyword#MADNESS} presence flag.
 */
public record MadnessCast(List<CastingCost> costs) implements CastingOption {

    public MadnessCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    public String manaCostString() {
        return getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
    }
}
