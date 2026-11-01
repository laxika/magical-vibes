package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "57")
public class DeathWatch extends Card {

    public DeathWatch() {
        // Enchant creature. When enchanted creature dies, its controller loses life equal to its
        // power and you gain life equal to its toughness. Both amounts are last-known information
        // baked at trigger time onto one stack entry (loss then gain).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect());
    }
}
