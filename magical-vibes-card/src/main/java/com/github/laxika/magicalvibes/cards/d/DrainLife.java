package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SoulBurnEffect;

@CardRegistration(set = "MIR", collectorNumber = "118")
@CardRegistration(set = "5ED", collectorNumber = "156")
@CardRegistration(set = "4ED", collectorNumber = "132")
@CardRegistration(set = "BTD", collectorNumber = "24")
@CardRegistration(set = "SUM", collectorNumber = "106")
public class DrainLife extends Card {

    public DrainLife() {
        setXColorRestrictions(ManaColor.BLACK);
        addEffect(EffectSlot.SPELL, new SoulBurnEffect());
    }
}
