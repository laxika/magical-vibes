package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "5DN", collectorNumber = "19")
public class SteelshapersGift extends Card {

    public SteelshapersGift() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
    }
}
