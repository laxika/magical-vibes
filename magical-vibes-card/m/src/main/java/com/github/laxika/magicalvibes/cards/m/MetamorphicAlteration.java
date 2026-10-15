package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureIsCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M19", collectorNumber = "60")
public class MetamorphicAlteration extends Card {

    public MetamorphicAlteration() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Metamorphic Alteration can only enchant a creature."
        ))
                // As this Aura enters, choose a creature.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCreatureOnEnterEffect())
                // Enchanted creature is a copy of the chosen creature.
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureIsCopyOfChosenCreatureEffect());
    }
}
