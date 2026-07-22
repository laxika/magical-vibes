package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "INR", collectorNumber = "232")
public class BloodhallPriest extends Card {

    public BloodhallPriest() {
        // Madness {1}{B}{R}
        addCastingOption(new MadnessCast("{1}{B}{R}"));

        // Whenever this creature enters or attacks, if you have no cards in hand,
        // this creature deals 2 damage to any target.
        ConditionalEffect emptyHandDamage = new ConditionalEffect(
                new ControllerHandEmpty(), new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, emptyHandDamage);
        addEffect(EffectSlot.ON_ATTACK, emptyHandDamage);
    }
}
