package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;

@CardRegistration(set = "ROE", collectorNumber = "101")
public class ConsumingVapors extends Card {

    public ConsumingVapors() {
        addEffect(EffectSlot.SPELL,
                new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(false, false));
    }
}
