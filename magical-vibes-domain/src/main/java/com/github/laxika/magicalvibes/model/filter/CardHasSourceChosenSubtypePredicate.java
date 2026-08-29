package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards carrying the creature subtype chosen by the source permanent.
 * Changeling cards match every creature subtype. Requires game state and a source card ID.
 *
 * @param creatureOnly whether the matching card must also be a creature
 */
public record CardHasSourceChosenSubtypePredicate(boolean creatureOnly) implements CardPredicate {

    public CardHasSourceChosenSubtypePredicate() {
        this(true);
    }
}
