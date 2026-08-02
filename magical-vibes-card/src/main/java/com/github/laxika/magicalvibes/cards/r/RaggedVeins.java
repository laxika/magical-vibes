package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "139")
public class RaggedVeins extends Card {

    public RaggedVeins() {
        // Amount 0 is a placeholder: the collector bakes in the damage just dealt.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE,
                        new EnchantedCreatureControllerLosesLifeEffect(0));
    }
}
