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
        addSpellEffect(new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addSpellEffect(SearchLibraryAndGraveyardForCardsToHandEffect.named("Nissa, Nature's Artisan"));
    }
}
