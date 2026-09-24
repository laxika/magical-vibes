package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsThatManyEffect;

@CardRegistration(set = "FRF", collectorNumber = "66")
@CardRegistration(set = "SLD", collectorNumber = "873")
@CardRegistration(set = "PIO", collectorNumber = "89")
@CardRegistration(set = "TLE", collectorNumber = "161")
public class DarkDeal extends Card {

    public DarkDeal() {
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsHandThenDrawsThatManyEffect(1));
    }
}
