package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "RTR", collectorNumber = "48")
public class Runewing extends Card {

    public Runewing() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
