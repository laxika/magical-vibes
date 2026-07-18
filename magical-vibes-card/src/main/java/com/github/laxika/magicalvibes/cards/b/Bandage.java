package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "9")
public class Bandage extends Card {

    public Bandage() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
