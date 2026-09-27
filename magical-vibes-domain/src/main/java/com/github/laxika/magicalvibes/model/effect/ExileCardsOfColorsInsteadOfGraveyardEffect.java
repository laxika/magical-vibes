package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static replacement effect that exiles cards of the listed colors instead of putting them into
 * any player's graveyard.
 */
public record ExileCardsOfColorsInsteadOfGraveyardEffect(Set<CardColor> colors)
        implements GlobalColorGraveyardExileReplacement {

    public ExileCardsOfColorsInsteadOfGraveyardEffect {
        colors = Set.copyOf(colors);
    }
}
