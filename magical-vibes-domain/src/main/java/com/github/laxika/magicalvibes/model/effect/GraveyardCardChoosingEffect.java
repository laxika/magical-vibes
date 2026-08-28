package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability interface for effects that pick target cards out of a graveyard as their ability
 * goes on the stack — a multi-card graveyard selection that a single {@link TargetSpec}
 * (one target, one category) cannot describe.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components. Trigger
 * collectors read it to route the ability to {@code GraveyardTargetingService} instead of pushing
 * the trigger straight onto the stack, without naming a concrete effect type.
 */
public interface GraveyardCardChoosingEffect extends CardEffect {

    /** Whether this effect's current configuration requires choosing individual graveyard cards. */
    default boolean choosesGraveyardCards() {
        return true;
    }

    /** The maximum number of graveyard cards the controller may choose ("up to N target cards"). */
    int graveyardChoiceMaxTargets();

    /** An optional restriction on the chosen cards ("creature cards"); {@code null} = any card. */
    default CardPredicate graveyardChoiceFilter() {
        return null;
    }

    /** Whether all selected cards must come from one graveyard. */
    default boolean singleGraveyard() {
        return false;
    }

    /** Whether the controller must choose exactly the maximum number of graveyard cards. */
    default boolean graveyardChoiceExactTargets() {
        return false;
    }
}
