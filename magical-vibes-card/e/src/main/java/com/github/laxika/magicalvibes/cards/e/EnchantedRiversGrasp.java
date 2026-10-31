package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOB", collectorNumber = "39")
public class EnchantedRiversGrasp extends Card {

    public EnchantedRiversGrasp() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.ENCHANTED))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RemoveAllCountersFromEnchantedCreatureEffect())
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(DoesntUntapEffect.enchanted());
    }
}
