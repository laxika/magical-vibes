package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GS1", collectorNumber = "3")
public class PurpleCrystalCrab extends Card {

    public PurpleCrystalCrab() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
