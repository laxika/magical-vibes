package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellsAndNonbasicLandsWithNontokenPermanentNamesCantBePlayedEffect;

@CardRegistration(set = "MMQ", collectorNumber = "14")
public class CorneredMarket extends Card {

    public CorneredMarket() {
        addEffect(EffectSlot.STATIC, new SpellsAndNonbasicLandsWithNontokenPermanentNamesCantBePlayedEffect());
    }
}
