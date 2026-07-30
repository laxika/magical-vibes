package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AVR", collectorNumber = "61")
public class InfiniteReflection extends Card {

    public InfiniteReflection() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Infinite Reflection can only enchant a creature."
        ))
                // When this Aura enters attached to a creature, each other nontoken creature you
                // control becomes a copy of that creature.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect())
                // Nontoken creatures you control enter as a copy of enchanted creature.
                .addEffect(EffectSlot.STATIC, new CreaturesEnterAsCopyOfSourceEffect(true, true));
    }
}
