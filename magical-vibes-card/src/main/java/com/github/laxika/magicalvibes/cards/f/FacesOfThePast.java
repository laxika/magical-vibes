package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapCreaturesSharingDyingCreatureTypeEffect;

@CardRegistration(set = "SCG", collectorNumber = "35")
public class FacesOfThePast extends Card {

    public FacesOfThePast() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new TapOrUntapCreaturesSharingDyingCreatureTypeEffect());
    }
}
