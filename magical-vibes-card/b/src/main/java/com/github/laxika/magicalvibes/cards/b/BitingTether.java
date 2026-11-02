package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "29")
public class BitingTether extends Card {

    public BitingTether() {
        target(TargetFilters.creature())
                // You control enchanted creature.
                .addStaticEffect(new ControlEnchantedCreatureEffect())

                // At the beginning of your upkeep, put a -1/-1 counter on enchanted creature.
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
