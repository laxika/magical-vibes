package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "147")
@CardRegistration(set = "DDH", collectorNumber = "16")
public class Spitemare extends Card {

    public Spitemare() {
        // Whenever this creature is dealt damage, it deals that much damage to any target.
        // The damage is the entry's event value; the target is chosen as the ability goes on the stack.
        addTrigger(Triggers.dealtDamage(), new DealDamageToAnyTargetEffect(new EventValue()));
    }
}
