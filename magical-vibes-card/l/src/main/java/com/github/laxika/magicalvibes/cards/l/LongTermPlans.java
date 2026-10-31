package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "SCG", collectorNumber = "38")
public class LongTermPlans extends Card {

    public LongTermPlans() {
        addSpellEffect(SearchLibraryEffect.topOfLibraryAtPosition(null, 2));
    }
}
