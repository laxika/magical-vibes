package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ONS", collectorNumber = "51")
public class RighteousCause extends Card {

    public RighteousCause() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new GainLifeEffect(1));
    }
}
