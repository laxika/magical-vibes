package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;

@CardRegistration(set = "FUT", collectorNumber = "14")
public class Saltskitter extends Card {

    public Saltskitter() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false));
    }
}
