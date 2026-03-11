package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "54")
public class Foresee extends Card {

    public Foresee() {
        addEffect(EffectSlot.SPELL, new ScryEffect(4));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
