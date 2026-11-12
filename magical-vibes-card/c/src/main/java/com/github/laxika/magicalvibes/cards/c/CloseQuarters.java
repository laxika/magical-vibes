package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "184")
public class CloseQuarters extends Card {

    public CloseQuarters() {
        // Whenever a creature you control becomes blocked, this enchantment deals 1 damage to any target.
        target(1, 1).addTrigger(Triggers.creatureYouControlBecomesBlocked(), new DealDamageToAnyTargetEffect(1));
    }
}
