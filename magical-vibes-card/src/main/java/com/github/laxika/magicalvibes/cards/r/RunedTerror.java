package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RunedTerrorEffect;

@CardRegistration(set = "MB2", collectorNumber = "371")
@CardRegistration(set = "MB2", collectorNumber = "610")
public class RunedTerror extends Card {

    public RunedTerror() {
        addEffect(EffectSlot.STATIC, new RunedTerrorEffect());
    }
}
