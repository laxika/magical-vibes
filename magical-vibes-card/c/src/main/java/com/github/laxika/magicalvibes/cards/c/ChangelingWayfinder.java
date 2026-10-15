package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ECL", collectorNumber = "1")
public class ChangelingWayfinder extends Card {

    public ChangelingWayfinder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand()), "Search your library for a basic land card?"));
    }
}
