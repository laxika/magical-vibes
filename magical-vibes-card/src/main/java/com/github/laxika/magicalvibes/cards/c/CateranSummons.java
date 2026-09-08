package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "126")
public class CateranSummons extends Card {

    public CateranSummons() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.MERCENARY)));
    }
}
