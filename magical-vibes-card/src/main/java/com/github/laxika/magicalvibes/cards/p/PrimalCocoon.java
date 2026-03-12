package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M11", collectorNumber = "191")
public class PrimalCocoon extends Card {

    public PrimalCocoon() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // At the beginning of your upkeep, put a +1/+1 counter on enchanted creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutPlusOnePlusOneCounterOnEnchantedCreatureEffect());

        // When enchanted creature attacks or blocks, sacrifice Primal Cocoon.
        addEffect(EffectSlot.ON_ATTACK, new SacrificeSelfEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeSelfEffect());
    }
}
