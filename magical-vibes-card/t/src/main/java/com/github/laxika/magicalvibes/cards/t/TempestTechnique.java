package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDC", collectorNumber = "16")
@CardRegistration(set = "TDC", collectorNumber = "56")
public class TempestTechnique extends Card {

    public TempestTechnique() {
        target(TargetFilters.creatureYouControl());

        PermanentCount enchantmentsYouControl = new PermanentCount(
                new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                enchantmentsYouControl, enchantmentsYouControl, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect(true));
    }
}
