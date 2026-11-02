package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "226")
public class ViashinoWeaponsmith extends Card {

    public ViashinoWeaponsmith() {
        // Whenever this creature becomes blocked by a creature, this creature gets +2/+2 until end of turn.
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new BoostSelfEffect(2, 2));
    }
}
