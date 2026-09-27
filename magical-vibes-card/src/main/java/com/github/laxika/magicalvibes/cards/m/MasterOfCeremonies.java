package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MasterOfCeremoniesEffect;

@CardRegistration(set = "NCC", collectorNumber = "18")
@CardRegistration(set = "NCC", collectorNumber = "119")
public class MasterOfCeremonies extends Card {

    public MasterOfCeremonies() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MasterOfCeremoniesEffect());
    }
}
