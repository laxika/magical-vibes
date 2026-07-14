package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "13")
public class RecumbentBliss extends Card {

    public RecumbentBliss() {
        // Enchant creature. Enchanted creature can't attack or block.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());

        // At the beginning of your upkeep, you may gain 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
