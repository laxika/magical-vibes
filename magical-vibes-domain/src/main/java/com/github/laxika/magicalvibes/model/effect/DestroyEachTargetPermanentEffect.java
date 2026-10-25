package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys each permanent targeted by the spell/ability (all of {@code entry.getTargetIds()}).
 * Bound to a single multi-target group, so it destroys every chosen target — used by
 * "Destroy X target nonblack creatures" (Dregs of Sorrow) via {@code targetX(...)}.
 *
 * <p>Snapshots the number of permanents actually destroyed this way onto the stack entry as its event
 * value, so a later effect on the same entry can reference "that many" via an {@code EventValue} amount
 * (The Elderspell's loyalty counters). Also stamps the controller of every permanent actually destroyed
 * onto {@code StackEntry.eventPlayerIds} (duplicates meaningful) and their last-known mana values onto
 * {@code eventManaValues}, for riders that need a per-player tally instead — see
 * {@link CreateTokenForEachDestroyedPermanentControllerEffect} (Descent of the Dragons). A permanent that
 * was destroyed but that a replacement such as Rest in Peace put somewhere other than a graveyard still
 * counts (Fumigate, Noxious Gearhulk and Break the Spell rulings), unless {@code destroyedCountScope} is
 * {@link DestroyedPermanentCountScope#DIED}, for "put into a graveyard this way" wordings (Volcanic
 * Eruption, Builder's Bane, Terastodon).
 *
 * @param cannotBeRegenerated whether the destroyed permanents cannot be regenerated
 * @param filter optional resolution-time filter; nonmatching targets are left untouched
 * @param destroyedCountScope {@link DestroyedPermanentCountScope#ALL} (default) or
 *                            {@link DestroyedPermanentCountScope#DIED}; the other scopes are not supported
 */
public record DestroyEachTargetPermanentEffect(boolean cannotBeRegenerated,
                                               PermanentPredicate filter,
                                               DestroyedPermanentCountScope destroyedCountScope) implements CardEffect {

    public DestroyEachTargetPermanentEffect {
        if (destroyedCountScope != DestroyedPermanentCountScope.ALL
                && destroyedCountScope != DestroyedPermanentCountScope.DIED) {
            throw new IllegalArgumentException("Unsupported destroyed count scope: " + destroyedCountScope);
        }
    }

    public DestroyEachTargetPermanentEffect(boolean cannotBeRegenerated, PermanentPredicate filter) {
        this(cannotBeRegenerated, filter, DestroyedPermanentCountScope.ALL);
    }

    public DestroyEachTargetPermanentEffect() {
        this(false, null);
    }

    /** Counts only the permanents put into a graveyard this way, for "put into a graveyard this way" riders. */
    public static DestroyEachTargetPermanentEffect countingPermanentsPutIntoAGraveyard() {
        return new DestroyEachTargetPermanentEffect(false, null, DestroyedPermanentCountScope.DIED);
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
