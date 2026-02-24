package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;

@CardRegistration(set = "SOM", collectorNumber = "56")
public class Blistergrub extends Card {

    public Blistergrub() {
        addEffect(EffectSlot.ON_DEATH, new EachOpponentLosesLifeEffect(2));
    }
}
