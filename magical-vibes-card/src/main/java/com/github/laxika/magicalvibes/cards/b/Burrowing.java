package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "170")
@CardRegistration(set = "4ED", collectorNumber = "180")
@CardRegistration(set = "SUM", collectorNumber = "140")
public class Burrowing extends Card {

    public Burrowing() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MOUNTAINWALK, GrantScope.ENCHANTED_CREATURE));
    }
}
