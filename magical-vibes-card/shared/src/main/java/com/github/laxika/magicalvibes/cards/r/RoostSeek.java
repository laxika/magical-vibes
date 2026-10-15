package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

public class RoostSeek extends Card {

    public RoostSeek() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(CardPredicateUtils.basicLand()));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
