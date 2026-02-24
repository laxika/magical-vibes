package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesXLifeAndControllerGainsLifeLostEffect;

@CardRegistration(set = "SOM", collectorNumber = "61")
public class Exsanguinate extends Card {

    public Exsanguinate() {
        addEffect(EffectSlot.SPELL, new EachOpponentLosesXLifeAndControllerGainsLifeLostEffect());
    }
}
