package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static evasion effect on attackers.
 * This creature can't be blocked as long as defending player controls a permanent matching the given predicate.
 *
 * <p>{@code landwalk} marks the snow-landwalk uses of this shape (Rime Dryad, Legions of Lim-Dûl),
 * which are landwalk abilities under CR 702.14a even though they are not landwalk {@code Keyword}s;
 * effects that switch landwalk off suppress those and leave the plain conditional ones alone.
 */
public record CantBeBlockedIfDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate,
                                                                    boolean landwalk)
        implements BlockabilityRestrictionEffect {

    public CantBeBlockedIfDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate) {
        this(defenderPermanentPredicate, false);
    }

    @Override
    public PermanentPredicate unblockableIfDefenderControls() {
        return defenderPermanentPredicate;
    }

    @Override
    public boolean unblockableIfDefenderControlsIsLandwalk() {
        return landwalk;
    }
}
