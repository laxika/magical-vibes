package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players other than the owner of the ability's source permanent.
 *
 * <p>This is source-relative: target validation must provide the source permanent id.</p>
 */
public record PlayerOtherThanSourceOwnerPredicate() implements PlayerPredicate {
}
