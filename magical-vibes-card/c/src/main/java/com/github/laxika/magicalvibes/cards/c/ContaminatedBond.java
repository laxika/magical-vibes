package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "132")
@CardRegistration(set = "9ED", collectorNumber = "120")
@CardRegistration(set = "MRD", collectorNumber = "61")
public class ContaminatedBond extends Card {

    public ContaminatedBond() {
        // The attack trigger is the Aura's own ability, so the Aura is its source and its controller is the
        // Aura's controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER))
                .addTrigger(Triggers.blocks(),
                        TriggerOptions.ONE_OR_MORE,
                        new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
