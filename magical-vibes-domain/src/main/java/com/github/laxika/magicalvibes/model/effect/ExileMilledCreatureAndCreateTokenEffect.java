package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Triggered ability: whenever a creature card is put into an opponent's graveyard from their
 * library, exile that card and create a creature token.
 * Used by Undead Alchemist.
 */
public record ExileMilledCreatureAndCreateTokenEffect(
        String tokenName,
        int tokenPower,
        int tokenToughness,
        CardColor tokenColor,
        List<CardSubtype> tokenSubtypes
) implements CardEffect {}
