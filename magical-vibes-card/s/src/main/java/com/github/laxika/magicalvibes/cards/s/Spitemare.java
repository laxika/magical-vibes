package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "147")
@CardRegistration(set = "DDH", collectorNumber = "16")
public class Spitemare extends Card {

    public Spitemare() {
        // Whenever this creature is dealt damage, it deals that much damage to any target.
        // The damage amount snapshots into xValue; the controller chooses the target when serviced.
        addTrigger(Triggers.dealtDamage(), new DealDamageToAnyTargetEffect(new XValue()));
    }
}
