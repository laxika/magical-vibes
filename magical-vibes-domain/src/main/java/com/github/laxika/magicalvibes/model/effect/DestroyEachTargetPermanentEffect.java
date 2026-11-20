package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys each permanent targeted by the spell/ability (all of {@code entry.getTargetIds()}) as one event.
 * Bound to a single multi-target group, so it destroys every chosen target — used by
 * "Destroy X target nonblack creatures" (Dregs of Sorrow) via {@code targetX(...)}.
 *
 * <p>The destruction is a {@code StepKind.DESTROY} step that records its departures, so a later step reads what it did
 * through {@code AmountThisWay}: "for each planeswalker destroyed this way" (The Elderspell; a permanent a
 * replacement such as Rest in Peace put elsewhere was still destroyed), {@code AmountThisWay.died} for "put into a
 * graveyard this way" (Volcanic Eruption, Builder's Bane, Terastodon), {@code controlledByYou()} for "its controller
 * creates" per permanent (Descent of the Dragons), {@code includingUnremoved()} for "for each of those permanents"
 * (Soul of Emancipation) and {@code AmountThisWay.destroyedManaValue()} for "the total mana value of permanents
 * destroyed this way" (Convert to Slime).
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
