package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "253")
@CardRegistration(set = "9ED", collectorNumber = "232")
@CardRegistration(set = "8ED", collectorNumber = "234")
@CardRegistration(set = "7ED", collectorNumber = "232")
@CardRegistration(set = "M19", collectorNumber = "169")
public class BlanchwoodArmor extends Card {

    public BlanchwoodArmor() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER),
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
