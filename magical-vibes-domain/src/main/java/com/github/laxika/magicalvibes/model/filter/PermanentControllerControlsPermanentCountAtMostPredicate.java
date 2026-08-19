package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent whose controller controls at most {@code maxCount} permanents matching
 * {@code countFilter}.
 *
 * <p>The count is based on the tested permanent's controller rather than the source of the
 * ability, which is needed for effects such as "basic lands each player controls have shroud as
 * long as that player controls three or fewer lands".
 */
public record PermanentControllerControlsPermanentCountAtMostPredicate(
        int maxCount, PermanentPredicate countFilter) implements PermanentPredicate {
}
