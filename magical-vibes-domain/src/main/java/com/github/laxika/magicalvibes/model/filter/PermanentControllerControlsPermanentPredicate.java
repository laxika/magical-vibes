package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent whose controller controls at least one permanent satisfying {@code filter}
 * (e.g. Seasinger's "target creature whose controller controls an Island" with a
 * {@link PermanentHasSubtypePredicate} for Island). Needs game data to resolve the target's
 * controller and scan that player's battlefield.
 */
public record PermanentControllerControlsPermanentPredicate(PermanentPredicate filter)
        implements PermanentPredicate {
}
