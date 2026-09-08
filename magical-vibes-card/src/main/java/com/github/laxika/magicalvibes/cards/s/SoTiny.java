package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ELD", collectorNumber = "64")
public class SoTiny extends Card {

    public SoTiny() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new FixedIfCondition(new GraveyardCardThreshold(7, null), -6, -2),
                new Fixed(0), GrantScope.ENCHANTED_CREATURE, true));
    }
}
