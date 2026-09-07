package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "146")
public class RiotSpikes extends Card {

    public RiotSpikes() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC,
                new AttachedBoostEffect(new Fixed(2), new Fixed(-1), GrantScope.ENCHANTED_CREATURE));
    }
}
