package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Capability interface for static effects that grant graveyard-activated abilities to cards in
 * their controller's graveyard (e.g. Sedris, the Traitor King and Mishra, Tamer of Mak Fawa).
 * Query/view code reads the granted ability through this FACT rather than branching on a concrete
 * effect type.
 */
public interface GraveyardAbilityGrantingEffect extends CardEffect {

    /**
     * The graveyard-activated ability granted to {@code card}, or {@code null} when this grant does
     * not apply to it. Most grants are card-independent; a card-derived cost (Varolz, the Scar-Striped
     * grants scavenge for a cost equal to the card's mana cost) reads it from {@code card}.
     */
    ActivatedAbility grantedGraveyardAbilityFor(Card card);

    /** Whether this grant applies to the supplied graveyard card. */
    default boolean appliesTo(Card card) {
        return true;
    }
}
