package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect;

@CardRegistration(set = "DSK", collectorNumber = "96")
public class FanaticOfTheHarrowing extends Card {

    public FanaticOfTheHarrowing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect());
    }
}
