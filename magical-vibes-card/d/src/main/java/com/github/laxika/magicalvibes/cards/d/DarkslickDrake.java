package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "30")
public class DarkslickDrake extends Card {

    public DarkslickDrake() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
