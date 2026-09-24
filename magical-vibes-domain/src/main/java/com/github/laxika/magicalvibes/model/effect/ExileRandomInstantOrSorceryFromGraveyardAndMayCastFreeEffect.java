package com.github.laxika.magicalvibes.model.effect;

/** Exiles a random instant or sorcery card with the given maximum mana value from the
 * controller's graveyard and offers it for immediate casting without paying its mana cost. */
public record ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect(int maxManaValue)
        implements CardEffect {}
