package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "JOU", collectorNumber = "25")
public class Skybind extends Card {

    public Skybind() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate()),
                "Target must be a nonenchantment permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, FlickerEffect.exileTargetReturnAtEndStep());

        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                FlickerEffect.exileTargetReturnAtEndStep());
    }
}
