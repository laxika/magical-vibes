package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the source permanent becomes the creature type chosen at resolution until end
 * of turn, optionally retaining its other creature types.
 */
public record SourceBecomesChosenSubtypeUntilEndOfTurnEffect(boolean retainOtherTypes) implements CardEffect {

    public SourceBecomesChosenSubtypeUntilEndOfTurnEffect() {
        this(false);
    }
}
