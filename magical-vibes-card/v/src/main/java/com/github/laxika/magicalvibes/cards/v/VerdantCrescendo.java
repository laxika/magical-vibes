package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndGraveyardForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "KLD", collectorNumber = "273")
public class VerdantCrescendo extends Card {

    public VerdantCrescendo() {
        // Two searches of the library before one "then shuffle" are one search (CR 701.23h): the land part neither
        // shuffles nor counts as a search of its own; the Nissa part's library search is recorded and shuffles.
        addSpellEffect(new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .withoutShuffle().asContinuationOfPreviousSearch());
        addSpellEffect(SearchLibraryAndGraveyardForCardsToHandEffect.named("Nissa, Nature's Artisan"));
    }
}
