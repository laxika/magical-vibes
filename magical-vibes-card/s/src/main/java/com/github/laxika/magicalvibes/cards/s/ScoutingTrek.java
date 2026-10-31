package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToTopEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "INV", collectorNumber = "210")
public class ScoutingTrek extends Card {

    public ScoutingTrek() {
        addSpellEffect(new SearchLibraryForCardsToTopEffect(CardPredicateUtils.basicLand()));
    }
}
