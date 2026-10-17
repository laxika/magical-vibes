package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches stack entries that are copies. With {@code castCopy} false (the default) it matches copies
 * that were put onto the stack without being cast; with {@code castCopy} true it matches copies of a
 * card that were cast (Isochron Scepter), which are spells but not cards.
 */
public record StackEntryIsCopyPredicate(boolean castCopy) implements StackEntryPredicate {

    public StackEntryIsCopyPredicate() {
        this(false);
    }
}
