package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "132")
public class FruitOfTheFirstTree extends Card {

    public FruitOfTheFirstTree() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect());
    }
}
