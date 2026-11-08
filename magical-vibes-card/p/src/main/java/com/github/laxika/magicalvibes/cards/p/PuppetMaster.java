package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "23")
@CardRegistration(set = "LEG", collectorNumber = "69")
public class PuppetMaster extends Card {

    public PuppetMaster() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.creatureDies(ObjectRelation.ENCHANTED),
                        new ReturnEnchantedCreatureToOwnerHandOnDeathEffect(
                                "{U}{U}{U}",
                                "Pay {U}{U}{U} to return Puppet Master to your hand?"));
    }
}
