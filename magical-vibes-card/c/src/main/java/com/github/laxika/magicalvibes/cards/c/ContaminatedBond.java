package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "132")
@CardRegistration(set = "9ED", collectorNumber = "120")
@CardRegistration(set = "MRD", collectorNumber = "61")
public class ContaminatedBond extends Card {

    public ContaminatedBond() {
        // The attack trigger is the Aura's own ability, so the Aura is its source and its controller is the
        // Aura's controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new EnchantedCreatureControllerLosesLifeEffect(3))
                .addEffect(EffectSlot.ON_BLOCK, new EnchantedCreatureControllerLosesLifeEffect(3), TriggerMode.ONCE_PER_BLOCK);
    }
}
