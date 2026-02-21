package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledLandCountEffect;

@CardRegistration(set = "10E", collectorNumber = "280")
public class MolimoMaroSorcerer extends Card {

    public MolimoMaroSorcerer() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledLandCountEffect());
    }
}
