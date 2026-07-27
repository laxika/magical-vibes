package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "70")
public class Erosion extends Card {

    public Erosion() {
        // Enchant land. At the beginning of the upkeep of enchanted land's controller,
        // destroy that land unless that player pays {1} or 1 life.
        target(TargetFilters.land()).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect("{1}", 1));
    }
}
