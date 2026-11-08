package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * When resolved, the targeted creature must block a permanent this turn if able, or only this combat when
 * {@code untilEndOfCombat} is true. The permanent is {@code sourcePermanentId} when set (an activated ability snapshots
 * its source as it is activated), else the one {@code blocked} names on the stack entry: the ability's source
 * ({@link PermanentReference#SOURCE}: "target creature blocks this creature", an attacker's own or temporarily granted
 * attack ability, Giant Ambush Beetle's enters ability) or the permanent the trigger event was about
 * ({@link PermanentReference#TRIGGERING}: an Equipment's "whenever equipped creature attacks, you may have target
 * creature block it", Grappling Hook), read as the ability resolves.
 */
public record MustBlockSourceEffect(UUID sourcePermanentId, PermanentPredicate targetRestriction,
                                    boolean untilEndOfCombat, PermanentReference blocked)
        implements CardEffect {

    public MustBlockSourceEffect {
        if (blocked != PermanentReference.SOURCE && blocked != PermanentReference.TRIGGERING) {
            throw new IllegalArgumentException("Blocks the source or the triggering permanent: " + blocked);
        }
    }

    public MustBlockSourceEffect(UUID sourcePermanentId, PermanentPredicate targetRestriction,
                                 boolean untilEndOfCombat) {
        this(sourcePermanentId, targetRestriction, untilEndOfCombat, PermanentReference.SOURCE);
    }

    public MustBlockSourceEffect(UUID sourcePermanentId) {
        this(sourcePermanentId, null, false);
    }

    public MustBlockSourceEffect(UUID sourcePermanentId, PermanentPredicate targetRestriction) {
        this(sourcePermanentId, targetRestriction, false);
    }

    /** "Target creature blocks it this turn if able", "it" the stack entry's triggering permanent. */
    public static MustBlockSourceEffect ofTriggeringPermanent() {
        return new MustBlockSourceEffect(null, null, false, PermanentReference.TRIGGERING);
    }

    /**
     * Only creatures can be forced to block; the CREATURE category enforces that, and the predicate
     * preserves the creature restriction on targeted-trigger candidates (carried on the spec predicate).
     */
    @Override
    public TargetSpec targetSpec() {
        PermanentPredicate restriction = targetRestriction == null
                ? new PermanentIsCreaturePredicate()
                : new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        targetRestriction));
        return TargetSpec.benign(TargetPredicates.creature(), restriction);
    }
}
