package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "68")
@CardRegistration(set = "ONE", collectorNumber = "105")
@CardRegistration(set = "A25", collectorNumber = "101")
@CardRegistration(set = "HA3", collectorNumber = "13")
public class PhyrexianObliterator extends Card {

    public PhyrexianObliterator() {
        // Whenever a source deals damage to this creature, that source's controller
        // sacrifices that many permanents.
        addTrigger(Triggers.dealtDamage(), new DamageSourceControllerSacrificesPermanentsEffect(0, null));
    }
}
