package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose card name is the name chosen by the source permanent. */
public record PermanentHasSourceChosenNamePredicate(boolean includePreviouslyChosenNames) implements PermanentPredicate {
    public PermanentHasSourceChosenNamePredicate() {
        this(false);
    }
}
