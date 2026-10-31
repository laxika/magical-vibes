package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "146")
@CardRegistration(set = "GK2", collectorNumber = "73")
public class RiotSpikes extends Card {

    public RiotSpikes() {
        target(TargetFilters.creature()).addStaticEffect(new AttachedBoostEffect(new Fixed(2), new Fixed(-1), GrantScope.ENCHANTED_CREATURE));
    }
}
