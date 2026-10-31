package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

public class RoostSeek extends Card {

    public RoostSeek() {
        addSpellEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand()));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
