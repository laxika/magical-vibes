package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: matching cards you own that aren't on the battlefield have madness. The madness
 * cost equals the card's mana cost (e.g. Falkenrath Gorger — Vampire creature cards).
 *
 * <p>Passively scanned from the {@code STATIC} slot via the {@link MadnessGrantingEffect}
 * capability. {@code GraveyardService.discardCard} consults
 * {@code GameQueryService.findGrantedMadnessCost} at discard time and snapshots the cost onto
 * {@link MadnessMayCastEffect} so the cast still works if the grant source leaves the battlefield
 * before the madness trigger resolves.
 */
public record GrantMadnessEqualToManaCostEffect(CardPredicate filter) implements MadnessGrantingEffect {

    @Override
    public CardPredicate madnessGrantFilter() {
        return filter;
    }
}
