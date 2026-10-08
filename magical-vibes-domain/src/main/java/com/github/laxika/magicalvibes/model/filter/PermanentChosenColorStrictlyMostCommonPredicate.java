package com.github.laxika.magicalvibes.model.filter;

/** Matches when the source's chosen color is strictly most common among its chosen player's nontoken permanents. */
public record PermanentChosenColorStrictlyMostCommonPredicate() implements PermanentPredicate {
}
