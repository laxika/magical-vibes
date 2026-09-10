package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "86")
public class TighteningCoils extends Card {

    public TighteningCoils() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(-6, 0, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new RemoveKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
    }
}
