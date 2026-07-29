package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "121")
@CardRegistration(set = "9ED", collectorNumber = "127")
@CardRegistration(set = "6ED", collectorNumber = "125")
public class Enfeeblement extends Card {

    public Enfeeblement() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE));
    }
}
