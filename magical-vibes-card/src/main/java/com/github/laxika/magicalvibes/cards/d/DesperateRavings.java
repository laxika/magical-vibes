package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;

@CardRegistration(set = "ISD", collectorNumber = "139")
public class DesperateRavings extends Card {

    public DesperateRavings() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new TargetPlayerRandomDiscardEffect(1));
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
