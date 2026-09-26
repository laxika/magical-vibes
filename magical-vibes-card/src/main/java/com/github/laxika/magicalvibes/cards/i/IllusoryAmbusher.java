package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "IMA", collectorNumber = "58")
@CardRegistration(set = "GN3", collectorNumber = "29")
@CardRegistration(set = "C15", collectorNumber = "12")
public class IllusoryAmbusher extends Card {

    public IllusoryAmbusher() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DrawCardEffect(new EventValue()));
    }
}
