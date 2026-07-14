package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "14")
public class SpiritOfTheHearth extends Card {

    public SpiritOfTheHearth() {
        addEffect(EffectSlot.STATIC, new GrantControllerHexproofEffect());
    }
}
