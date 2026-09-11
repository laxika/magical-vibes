package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "OPC2", collectorNumber = "4")
public class MutualEpiphany extends Card {

    public MutualEpiphany() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new EachPlayerDrawsCardEffect(4));
    }
}
