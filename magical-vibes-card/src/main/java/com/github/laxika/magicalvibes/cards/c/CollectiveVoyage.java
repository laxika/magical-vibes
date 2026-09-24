package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenSearchesBasicLandsEffect;

@CardRegistration(set = "SLD", collectorNumber = "1009")
public class CollectiveVoyage extends Card {

    public CollectiveVoyage() {
        addEffect(EffectSlot.SPELL, new EachPlayerPaysAnyManaThenSearchesBasicLandsEffect());
    }
}
