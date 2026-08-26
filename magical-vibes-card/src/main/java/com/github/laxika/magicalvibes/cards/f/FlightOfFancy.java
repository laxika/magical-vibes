package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "49")
public class FlightOfFancy extends Card {

    public FlightOfFancy() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(2))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
    }
}
