package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability interface for effects that pick "up to N target cards" out of a graveyard as their
 * ability goes on the stack — a multi-card graveyard selection that a single {@link TargetSpec}
 * (one target, one category) cannot describe.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components. Trigger
 * collectors read it to route the ability to {@code GraveyardTargetingService} instead of pushing
 * the trigger straight onto the stack, without naming a concrete effect type.
 */
public interface GraveyardCardChoosingEffect extends CardEffect {

    /** The maximum number of graveyard cards the controller may choose ("up to N target cards"). */
    int graveyardChoiceMaxTargets();

    /** An optional restriction on the chosen cards ("creature cards"); {@code null} = any card. */
    default CardPredicate graveyardChoiceFilter() {
        return null;
    }
}
