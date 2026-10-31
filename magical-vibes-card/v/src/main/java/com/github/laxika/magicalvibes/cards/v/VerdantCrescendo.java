package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndGraveyardForNamedCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "273")
public class VerdantCrescendo extends Card {

    public VerdantCrescendo() {
        addSpellEffect(new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addSpellEffect(new SearchLibraryAndGraveyardForNamedCardsToHandEffect(
                List.of("Nissa, Nature's Artisan")));
    }
}
