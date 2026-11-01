package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "65")
public class BanewaspAffliction extends Card {

    public BanewaspAffliction() {
        // Enchant creature. When enchanted creature dies, that creature's controller loses life equal
        // to its toughness. The amount is baked from the dying creature's toughness at trigger time.
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new EnchantedCreatureControllerLosesLifeEffect(0));
    }
}
