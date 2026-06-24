package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "NPH", collectorNumber = "145")
public class MycosynthWellspring extends Card {

    public MycosynthWellspring() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryForCardsToHandEffect(CardPredicateUtils.basicLand()),
                        "Search your library for a basic land card?"));
        addEffect(EffectSlot.ON_DEATH,
                new MayEffect(new SearchLibraryForCardsToHandEffect(CardPredicateUtils.basicLand()),
                        "Search your library for a basic land card?"));
    }
}
