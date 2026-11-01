package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "25")
public class Skybind extends Card {

    public Skybind() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate()),
                "Target must be a nonenchantment permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, FlickerEffect.exileTargetReturnAtEndStep());

        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                FlickerEffect.exileTargetReturnAtEndStep());
    }
}
