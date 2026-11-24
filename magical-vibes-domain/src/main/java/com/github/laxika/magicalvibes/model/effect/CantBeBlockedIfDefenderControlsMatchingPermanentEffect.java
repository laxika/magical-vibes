package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static evasion effect on attackers: this creature can't be blocked as long as the defending player controls a
 * permanent matching {@code defenderPermanentPredicate}, or, with {@link #whileDefendingPlayer(Condition)}, as long as
 * {@code defendingPlayerCondition} holds for the defending player. The condition is judged with the defending player
 * (CR 508.5) as the condition context's target, as blockers are declared (CR 509.1b): "as long as defending player
 * has a rad counter" is {@code TargetPlayerHasRadCounters}, "unless defending player controls …" a
 * {@code NotCondition}. Exactly one of the two is set.
 *
 * <p>{@code landwalk} marks the snow-landwalk uses of this shape (Rime Dryad, Legions of Lim-Dûl),
 * which are landwalk abilities under CR 702.14a even though they are not landwalk {@code Keyword}s;
 * effects that switch landwalk off suppress those and leave the plain conditional ones alone.
 */
public record CantBeBlockedIfDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate,
                                                                    boolean landwalk,
                                                                    Condition defendingPlayerCondition)
        implements BlockabilityRestrictionEffect {

    public CantBeBlockedIfDefenderControlsMatchingPermanentEffect {
        if ((defenderPermanentPredicate == null) == (defendingPlayerCondition == null)) {
            throw new IllegalArgumentException("exactly one of a permanent predicate and a defending-player condition");
        }
        if (landwalk && defenderPermanentPredicate == null) {
            throw new IllegalArgumentException("a landwalk ability names a permanent the defending player controls");
        }
    }

    public CantBeBlockedIfDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate,
                                                                 boolean landwalk) {
        this(defenderPermanentPredicate, landwalk, null);
    }

    public CantBeBlockedIfDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate) {
        this(defenderPermanentPredicate, false, null);
    }

    /** "This creature can't be blocked as long as defending player [condition]" (judged for the defending player). */
    public static CantBeBlockedIfDefenderControlsMatchingPermanentEffect whileDefendingPlayer(Condition condition) {
        return new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(null, false, condition);
    }

    @Override
    public PermanentPredicate unblockableIfDefenderControls() {
        return defenderPermanentPredicate;
    }

    @Override
    public Condition unblockableWhileDefendingPlayer() {
        return defendingPlayerCondition;
    }

    @Override
    public boolean unblockableIfDefenderControlsIsLandwalk() {
        return landwalk;
    }
}
