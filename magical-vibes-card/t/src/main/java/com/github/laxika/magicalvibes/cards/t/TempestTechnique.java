package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "16")
@CardRegistration(set = "TDC", collectorNumber = "56")
public class TempestTechnique extends Card {

    public TempestTechnique() {
        target(TargetFilters.creatureYouControl());

        PermanentCount enchantmentsYouControl = new PermanentCount(
                new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new DynamicStaticBoostEffect(
                enchantmentsYouControl, enchantmentsYouControl, GrantScope.ENCHANTED_CREATURE));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect(true));
    }
}
