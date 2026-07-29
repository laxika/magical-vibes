package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent whose controller controls at least one permanent satisfying {@code filter}
 * (e.g. Seasinger's "target creature whose controller controls an Island" with a
 * {@link PermanentHasSubtypePredicate} for Island). Needs game data to resolve the target's
 * controller and scan that player's battlefield.
 *
 * <p>{@code excludeSelf} skips the permanent being tested during the scan, which is what "as long
 * as its controller controls <em>another</em> creature" needs (Favorable Destiny).</p>
 */
public record PermanentControllerControlsPermanentPredicate(PermanentPredicate filter, boolean excludeSelf)
        implements PermanentPredicate {

    public PermanentControllerControlsPermanentPredicate(PermanentPredicate filter) {
        this(filter, false);
    }
}
