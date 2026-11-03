package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "2")
public class AngelOfDeliverance extends Card {

    public AngelOfDeliverance() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.dealsDamage(),
                        TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                        new ExileTargetPermanentEffect());
    }
}
