package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "34")
public class ShoulderToShoulder extends Card {

    public ShoulderToShoulder() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
