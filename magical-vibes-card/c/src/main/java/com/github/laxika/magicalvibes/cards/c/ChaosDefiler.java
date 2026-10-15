package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffect;

@CardRegistration(set = "40K", collectorNumber = "110")
public class ChaosDefiler extends Card {

    public ChaosDefiler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffect());
        addEffect(EffectSlot.ON_DEATH,
                new ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffect());
    }
}
