package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import java.util.List;

/**
 * One search of the controller's library for a card of each listed colour (one card per colour, in the
 * given order), putting the found cards into the graveyard together after the last pick, then
 * shuffling. A card of several colours may fill any one of its colours but never two picks. The
 * searcher may always fail to find any of the picks, and a colour with no matching card left in the
 * library is skipped. The whole effect is one library search however many cards are found. Used by
 * Lotuslight Dancers.
 */
public record SearchLibraryForOneCardOfEachColorToGraveyardEffect(List<CardColor> colors) implements CardEffect {

    public SearchLibraryForOneCardOfEachColorToGraveyardEffect {
        colors = List.copyOf(colors);
    }
}
