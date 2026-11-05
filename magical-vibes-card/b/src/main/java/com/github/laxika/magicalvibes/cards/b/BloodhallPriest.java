package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "INR", collectorNumber = "232")
@CardRegistration(set = "EMN", collectorNumber = "181")
@CardRegistration(set = "SIR", collectorNumber = "231")
@CardRegistration(set = "C19", collectorNumber = "188")
public class BloodhallPriest extends Card {

    public BloodhallPriest() {
        // Madness {1}{B}{R}
        addCastingOption(new MadnessCast("{1}{B}{R}"));

        // Whenever this creature enters or attacks, if you have no cards in hand,
        // this creature deals 2 damage to any target.
        TriggerOptions emptyHand = TriggerOptions.DEFAULT.withInterveningIf(new ControllerHandEmpty());
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), emptyHand, damage);
        addTrigger(Triggers.attacks(), emptyHand, damage);
    }
}
