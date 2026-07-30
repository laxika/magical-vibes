package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "143")
public class GoblinWarPaint extends Card {

    public GoblinWarPaint() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE));
    }
}
