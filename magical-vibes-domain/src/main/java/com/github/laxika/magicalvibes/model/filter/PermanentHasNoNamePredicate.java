package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose current characteristics give them no name: face-down permanents and
 * copies of them (CR 708.2, CR 707.2). Used by "creatures that don't have a name" (Pompous Gadabout).
 */
public record PermanentHasNoNamePredicate() implements PermanentPredicate {
}
