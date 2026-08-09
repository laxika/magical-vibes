package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "93")
public class MarkOfFury extends Card {

    public MarkOfFury() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.END_STEP_TRIGGERED, ReturnToHandEffect.self());
    }
}
