package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "Destroy all [permanents]" with a rider that acts once per destroyed permanent for its controller ("the controller
 * of each of those artifacts gains life equal to its mana value", Seeds of Innocence; "for each land destroyed this
 * way, this deals 1 damage to that land's controller unless they pay {2}", Stench of Evil). A plain wipe is
 * {@link DestroyTargetPermanentEffect#all}.
 *
 * <p>Every permanent matching {@code filter} on every battlefield is destroyed as one event, recorded as a
 * {@code StepKind.DESTROY} step with its departures; then {@code thenEffect} resolves with the destroyed count on
 * {@code StackEntry.eventValue} and the destroyed permanents' controllers and mana values on the entry's event lists,
 * against the effect's controller.
 *
 * @param filter              which permanents are destroyed
 * @param cannotBeRegenerated when {@code true} the destruction can't be prevented by regeneration
 * @param thenEffect          the per-destroyed-permanent rider resolved after the destruction
 */
public record DestroyAllPermanentsEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated,
        CardEffect thenEffect
) implements BoardWipeEffect {

    public DestroyAllPermanentsEffect {
        Objects.requireNonNull(thenEffect, "thenEffect (a plain wipe is DestroyTargetPermanentEffect.all)");
    }

    /** Board wipe with a per-destroyed-permanent rider ("For each land destroyed this way, its controller …"). */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, CardEffect thenEffect) {
        this(filter, false, thenEffect);
    }

    /** Destroy-all always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
