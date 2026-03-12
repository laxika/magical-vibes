package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "70")
public class Preordain extends Card {

    public Preordain() {
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
