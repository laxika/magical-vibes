package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "29")
public class SinisterPossession extends Card {

    public SinisterPossession() {
        // Enchant creature. Whenever enchanted creature attacks or blocks, its controller loses 2 life.
        // The aura trigger collector bakes the enchanted creature's controller into the stack copy.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new EnchantedCreatureControllerLosesLifeEffect(2))
                .addEffect(EffectSlot.ON_BLOCK, new EnchantedCreatureControllerLosesLifeEffect(2));
    }
}
