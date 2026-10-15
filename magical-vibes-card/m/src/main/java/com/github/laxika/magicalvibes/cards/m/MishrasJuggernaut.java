package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "BRO", collectorNumber = "161")
public class MishrasJuggernaut extends Card {

    public MishrasJuggernaut() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addUnearth("{5}{R}");
    }
}
