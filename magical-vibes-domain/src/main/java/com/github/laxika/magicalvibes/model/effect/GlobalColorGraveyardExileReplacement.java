package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Capability for a static replacement that exiles matching cards instead of putting them into
 * any player's graveyard. The graveyard service evaluates battlefield colors through the current
 * layered characteristics and non-battlefield colors through the card's effective colors.
 */
public interface GlobalColorGraveyardExileReplacement extends CardEffect {

    Set<CardColor> colors();
}
