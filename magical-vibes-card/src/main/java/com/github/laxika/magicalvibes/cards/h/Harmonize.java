package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "PLC", collectorNumber = "149")
@CardRegistration(set = "DD1", collectorNumber = "22")
@CardRegistration(set = "DDD", collectorNumber = "21")
@CardRegistration(set = "EVG", collectorNumber = "22")
@CardRegistration(set = "GVL", collectorNumber = "21")
@CardRegistration(set = "EMA", collectorNumber = "170")
@CardRegistration(set = "MM3", collectorNumber = "128")
@CardRegistration(set = "DDS", collectorNumber = "46")
@CardRegistration(set = "MB1", collectorNumber = "151")
@CardRegistration(set = "TSR", collectorNumber = "208")
@CardRegistration(set = "STA", collectorNumber = "52")
@CardRegistration(set = "ECC", collectorNumber = "111")
@CardRegistration(set = "C13", collectorNumber = "148")
@CardRegistration(set = "TMC", collectorNumber = "51")
@CardRegistration(set = "CMD", collectorNumber = "158")
public class Harmonize extends Card {

    public Harmonize() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
