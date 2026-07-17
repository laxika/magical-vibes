package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Capability interface for static effects that grant a graveyard-activated ability to creature cards
 * in their controller's graveyard (e.g. Sedris, the Traitor King: "Each creature card in your graveyard
 * has unearth {2}{B}."). Query/view code reads the granted ability through this FACT rather than
 * branching on a concrete effect type.
 */
public interface GraveyardAbilityGrantingEffect extends CardEffect {

    /** The graveyard-activated ability granted to each owned creature card in the graveyard. */
    ActivatedAbility grantedGraveyardAbility();
}
