package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "133")
@CardRegistration(set = "INR", collectorNumber = "147")
@CardRegistration(set = "INR", collectorNumber = "397")
@CardRegistration(set = "EMA", collectorNumber = "121")
@CardRegistration(set = "C19", collectorNumber = "135")
public class BurningVengeance extends Card {

    public BurningVengeance() {
        // Whenever you cast a spell from your graveyard, Burning Vengeance deals 2 damage to any target.
        addTrigger(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD), new DealDamageToAnyTargetEffect(2));
    }
}
