package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHR", collectorNumber = "23")
@CardRegistration(set = "LEG", collectorNumber = "69")
public class PuppetMaster extends Card {

    public PuppetMaster() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnEnchantedCreatureToOwnerHandOnDeathEffect(
                                "{U}{U}{U}",
                                "Pay {U}{U}{U} to return Puppet Master to your hand?"));
    }
}
