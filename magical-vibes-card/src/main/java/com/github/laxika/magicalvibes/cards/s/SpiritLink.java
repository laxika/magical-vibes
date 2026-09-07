package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "45")
@CardRegistration(set = "LEG", collectorNumber = "37")
@CardRegistration(set = "9ED", collectorNumber = "47")
@CardRegistration(set = "8ED", collectorNumber = "47")
@CardRegistration(set = "7ED", collectorNumber = "47")
@CardRegistration(set = "6ED", collectorNumber = "43")
@CardRegistration(set = "5ED", collectorNumber = "64")
@CardRegistration(set = "4ED", collectorNumber = "51")
public class SpiritLink extends Card {

    public SpiritLink() {
        // Enchant creature
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
