package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "23")
public class GuardDuty extends Card {

    public GuardDuty() {
        // Enchant creature
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE));
    }
}
