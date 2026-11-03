package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "103")
public class Pyroconvergence extends Card {

    public Pyroconvergence() {
        // Whenever you cast a multicolored spell, this enchantment deals 2 damage to any target.
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()),
                new DealDamageToAnyTargetEffect(2));
    }
}
