package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

public class Mountain extends Card {

    public Mountain() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
    }
}
