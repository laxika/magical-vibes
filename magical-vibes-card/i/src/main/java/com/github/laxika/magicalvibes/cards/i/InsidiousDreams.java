package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToTopEffect;

@CardRegistration(set = "TOR", collectorNumber = "66")
public class InsidiousDreams extends Card {

    public InsidiousDreams() {
        addCastingCost(new DiscardXCardsCost());
        addSpellEffect(SearchLibraryForCardsToTopEffect.exact(new XValue()));
    }
}
