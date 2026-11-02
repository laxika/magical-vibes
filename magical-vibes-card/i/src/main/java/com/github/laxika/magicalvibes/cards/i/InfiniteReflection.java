package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "61")
@CardRegistration(set = "C14", collectorNumber = "114")
public class InfiniteReflection extends Card {

    public InfiniteReflection() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Infinite Reflection can only enchant a creature."
        ))
                // When this Aura enters attached to a creature, each other nontoken creature you
                // control becomes a copy of that creature.
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect())
                // Nontoken creatures you control enter as a copy of enchanted creature.
                .addStaticEffect(new CreaturesEnterAsCopyOfSourceEffect(true, true));
    }
}
