package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "77")
public class RelicPutrescence extends Card {

    public RelicPutrescence() {
        target(TargetFilters.artifact()).addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new GivePoisonCountersEffect(1, PoisonRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
