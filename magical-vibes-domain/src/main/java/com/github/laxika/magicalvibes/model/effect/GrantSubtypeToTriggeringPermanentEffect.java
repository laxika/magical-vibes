package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot effect that permanently grants a subtype to the permanent whose event produced the
 * triggered ability ("that creature becomes a [subtype] in addition to its other types"). It never
 * targets: the permanent is read from the triggering permanent of the resolving ability.
 *
 * @param subtype the subtype added to the triggering permanent's granted subtypes
 */
public record GrantSubtypeToTriggeringPermanentEffect(CardSubtype subtype) implements CardEffect {

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
