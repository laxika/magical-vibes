package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "7ED", collectorNumber = "70")
public class DelusionsOfMediocrity extends Card {

    public DelusionsOfMediocrity() {
        // "When this enchantment enters, you gain 10 life."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(10));

        // "When this enchantment leaves the battlefield, you lose 10 life."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new LoseLifeEffect(10));
    }
}
