package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "70")
@CardRegistration(set = "ULG", collectorNumber = "30")
public class DelusionsOfMediocrity extends Card {

    public DelusionsOfMediocrity() {
        // "When this enchantment enters, you gain 10 life."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(10));

        // "When this enchantment leaves the battlefield, you lose 10 life."
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new LoseLifeEffect(10));
    }
}
