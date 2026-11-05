package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.effect.StateTriggerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * A state trigger: the ability triggers when a game state is true rather than when an event happens, and doesn't
 * trigger again until it has left the stack (CR 603.8). The components are the game state; every one that is set must
 * hold. Build it through {@link Triggers}: "when you have no cards in hand" is {@code Triggers.state((gameData,
 * source, controllerId) -> …)}, "when this creature has flying" {@code Triggers.sourceState(hasFlying)}, "when you
 * control no Islands" {@code Triggers.battlefieldHasAtMost(0, islandYouControl)}. {@link Triggers#state()}, with no
 * game state, is only the trigger the state-trigger reader watches; a card's ability needs a game state.
 *
 * @param predicate                    a condition over the whole game, or {@code null}
 * @param sourcePredicate              a condition on the source permanent, checked through the layer-aware
 *                                     predicate evaluator (so keyword, color and type grants count), or {@code null}.
 *                                     When it is set, {@code battlefieldPredicate} isn't read
 * @param battlefieldPredicate         the permanents counted across all battlefields, or {@code null}
 * @param maximumBattlefieldMatches    the most permanents that may match {@code battlefieldPredicate}
 * @param minimumBattlefieldMatches    the fewest permanents that must match {@code battlefieldPredicate}
 * @param referencedPermanentPredicate a permanent that must exist, the first of which the ability refers to (not a
 *                                     target): "when an opponent controls a creature with power 4 or greater", or
 *                                     {@code null}
 */
public record StateTrigger(StateTriggerPredicate predicate, PermanentPredicate sourcePredicate,
                           PermanentPredicate battlefieldPredicate, int maximumBattlefieldMatches,
                           int minimumBattlefieldMatches, PermanentPredicate referencedPermanentPredicate)
        implements Trigger {

    /** The trigger the state-trigger reader watches, with no game state. */
    public StateTrigger() {
        this(null, null, null, 0, 0, null);
    }

    /** Whether this trigger names a game state, as a card's state trigger must. */
    public boolean hasState() {
        return predicate != null || sourcePredicate != null || battlefieldPredicate != null;
    }

    /** This game state, where the source permanent must also match {@code sourcePredicate}. */
    public StateTrigger withSource(PermanentPredicate sourcePredicate) {
        Objects.requireNonNull(sourcePredicate, "sourcePredicate");
        if (this.sourcePredicate != null) {
            throw new IllegalStateException("This state trigger already has a source condition: " + this);
        }
        return new StateTrigger(predicate, sourcePredicate, battlefieldPredicate, maximumBattlefieldMatches,
                minimumBattlefieldMatches, referencedPermanentPredicate);
    }

    /** This game state, where at most {@code maximumMatches} permanents may match {@code battlefieldPredicate}. */
    public StateTrigger withBattlefieldAtMost(int maximumMatches, PermanentPredicate battlefieldPredicate) {
        Objects.requireNonNull(battlefieldPredicate, "battlefieldPredicate");
        if (this.battlefieldPredicate != null) {
            throw new IllegalStateException("This state trigger already counts permanents: " + this);
        }
        return new StateTrigger(predicate, sourcePredicate, battlefieldPredicate, maximumMatches, 0,
                referencedPermanentPredicate);
    }

    /**
     * This game state, where a permanent matching {@code referencedPermanentPredicate} must exist; the ability refers
     * to the first one.
     */
    public StateTrigger referencing(PermanentPredicate referencedPermanentPredicate) {
        Objects.requireNonNull(referencedPermanentPredicate, "referencedPermanentPredicate");
        if (this.referencedPermanentPredicate != null) {
            throw new IllegalStateException("This state trigger already refers to a permanent: " + this);
        }
        return new StateTrigger(predicate, sourcePredicate, battlefieldPredicate, maximumBattlefieldMatches,
                minimumBattlefieldMatches, referencedPermanentPredicate);
    }

    /** The trigger the state-trigger reader reads this one's abilities by: {@link Triggers#state()}. */
    @Override
    public Trigger watchKey() {
        return hasState() || referencedPermanentPredicate != null ? new StateTrigger() : this;
    }

}
