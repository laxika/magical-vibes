package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardForColorSourceDamageEffect;

@CardRegistration(set = "40K", collectorNumber = "83")
public class TheRedTerror extends Card {

    public TheRedTerror() {
        addEffect(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE,
                new PutCountersOnSourceCardForColorSourceDamageEffect(
                        CardColor.RED, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
