package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "29")
public class SinisterPossession extends Card {

    public SinisterPossession() {
        // Enchant creature. Whenever enchanted creature attacks or blocks, its controller loses 2 life.
        // The aura trigger collector bakes the enchanted creature's controller into the stack copy.
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER))
                .addTrigger(Triggers.blocks(),
                        TriggerOptions.ONE_OR_MORE,
                        new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
