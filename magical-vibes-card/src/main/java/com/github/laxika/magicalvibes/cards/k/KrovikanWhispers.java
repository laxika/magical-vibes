package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "39")
public class KrovikanWhispers extends Card {

    public KrovikanWhispers() {
        target(TargetFilters.creature())
                // You control enchanted creature.
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect())
                // Cumulative upkeep {U} or {B}.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{U/B}"));

        // When this Aura is put into a graveyard from the battlefield, you lose 2 life for each age
        // counter on it.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new LoseLifeEffect(new Scaled(new CountersOnSource(CounterType.AGE), 2),
                        LoseLifeRecipient.CONTROLLER));
    }
}
