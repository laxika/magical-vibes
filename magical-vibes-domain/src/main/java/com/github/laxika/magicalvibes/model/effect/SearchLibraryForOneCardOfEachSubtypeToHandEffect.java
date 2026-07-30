package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import java.util.List;

/**
 * Searches the controller's library for one card of each listed subtype (one card per subtype, in the
 * given order). Each found card is revealed and put into the controller's hand; the library is
 * shuffled once after every subtype has been searched. A subtype with no matching card in the library
 * is skipped, and the controller may always fail to find. Used by Gem of Becoming (Island, Swamp,
 * Mountain).
 */
public record SearchLibraryForOneCardOfEachSubtypeToHandEffect(List<CardSubtype> subtypes) implements CardEffect {

    public SearchLibraryForOneCardOfEachSubtypeToHandEffect {
        subtypes = List.copyOf(subtypes);
    }
}
