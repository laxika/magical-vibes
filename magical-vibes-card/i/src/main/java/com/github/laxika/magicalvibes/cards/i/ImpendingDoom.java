package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "139")
public class ImpendingDoom extends Card {

    public ImpendingDoom() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new MustAttackEffect())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new DealDamageToEnchantedPermanentControllerEffect(3));
    }
}
