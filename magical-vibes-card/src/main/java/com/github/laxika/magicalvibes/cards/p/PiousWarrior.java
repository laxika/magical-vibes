package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MMQ", collectorNumber = "34")
public class PiousWarrior extends Card {

    public PiousWarrior() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF, new GainLifeEffect(new EventValue()));
    }
}
