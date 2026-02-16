package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

public class RevivingDose extends Card {

    public RevivingDose() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
