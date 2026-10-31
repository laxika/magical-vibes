package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandAndCardToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "SOI", collectorNumber = "205")
public class ForkInTheRoad extends Card {

    public ForkInTheRoad() {
        addSpellEffect(new SearchLibraryForCardToHandAndCardToGraveyardEffect(
                CardPredicateUtils.basicLand(), true, true));
    }
}
