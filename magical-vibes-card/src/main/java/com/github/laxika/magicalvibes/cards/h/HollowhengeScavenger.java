package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;

@CardRegistration(set = "ISD", collectorNumber = "188")
public class HollowhengeScavenger extends Card {

    public HollowhengeScavenger() {
        // Morbid — When Hollowhenge Scavenger enters the battlefield,
        // if a creature died this turn, you gain 5 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MorbidConditionalEffect(
                new GainLifeEffect(5)
        ));
    }
}
