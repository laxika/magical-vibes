package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "CSP", collectorNumber = "32")
public class Drelnoch extends Card {

    public Drelnoch() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new MayEffect(new DrawCardEffect(2), "Draw two cards?"));
    }
}
