package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Erupting Dreadwolf — back face of Smoldering Werewolf.
 * Whenever this creature attacks, it deals 2 damage to any target.
 */
public class EruptingDreadwolf extends Card {

    public EruptingDreadwolf() {
        // Whenever this creature attacks, it deals 2 damage to any target.
        addTrigger(Triggers.attacks(), new DealDamageToAnyTargetEffect(2));
    }
}
