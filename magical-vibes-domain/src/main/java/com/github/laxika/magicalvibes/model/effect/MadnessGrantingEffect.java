package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability interface for static effects that grant madness to matching cards you own that aren't
 * on the battlefield (e.g. Falkenrath Gorger). Query/discard code reads the filter through this FACT
 * rather than branching on a concrete effect type.
 *
 * <p>The madness cost equals the card's mana cost. Checked at discard time by
 * {@code GameQueryService.findGrantedMadnessCost}.
 */
public interface MadnessGrantingEffect extends CardEffect {

    /** Cards matching this filter gain madness while the source is on the battlefield. */
    CardPredicate madnessGrantFilter();
}
