package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "132")
public class FruitOfTheFirstTree extends Card {

    public FruitOfTheFirstTree() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect());
    }
}
