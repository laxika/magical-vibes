package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;

@CardRegistration(set = "JOU", collectorNumber = "75")
public class MasterOfTheFeast extends Card {

    public MasterOfTheFeast() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EachOtherPlayerDrawsCardEffect(1));
    }
}
