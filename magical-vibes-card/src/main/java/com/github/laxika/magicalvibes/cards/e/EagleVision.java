package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "17")
public class EagleVision extends Card {

    public EagleVision() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{U}")), new Freerunning(), false));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
