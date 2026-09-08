package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys each permanent targeted by the spell/ability (all of {@code entry.getTargetIds()}).
 * Bound to a single multi-target group, so it destroys every chosen target — used by
 * "Destroy X target nonblack creatures" (Dregs of Sorrow) via {@code targetX(...)}.
 *
 * <p>Snapshots the number of permanents actually put into a graveyard this way onto the stack
 * entry as its event value, so a later effect on the same entry can reference "that many" via an
 * {@code EventValue} amount (Volcanic Eruption's mass damage). Also stamps the controller of every
 * permanent actually destroyed onto {@code StackEntry.eventPlayerIds} (duplicates meaningful), for
 * riders that need a per-player tally instead — see
 * {@link DealDamageToEachDestroyedPermanentControllerEffect} (Builder's Bane).
 *
 * @param cannotBeRegenerated whether the destroyed permanents cannot be regenerated
 * @param filter optional resolution-time filter; nonmatching targets are left untouched
 */
public record DestroyEachTargetPermanentEffect(boolean cannotBeRegenerated,
                                               PermanentPredicate filter) implements CardEffect {

    public DestroyEachTargetPermanentEffect() {
        this(false, null);
    }

    public DestroyEachTargetPermanentEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null);
    }

    public DestroyEachTargetPermanentEffect(PermanentPredicate filter) {
        this(false, filter);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
