package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose physical card is designated as a commander in the game. */
public record PermanentIsCommanderPredicate(boolean ownedBySourceController) implements PermanentPredicate {

    public PermanentIsCommanderPredicate() {
        this(false);
    }
}
