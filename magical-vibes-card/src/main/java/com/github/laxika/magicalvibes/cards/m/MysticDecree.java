package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "HML", collectorNumber = "34")
public class MysticDecree extends Card {

    public MysticDecree() {
        // All creatures lose flying and islandwalk.
        // Two continuous layer-6 keyword removals over every creature on the battlefield.
        addEffect(EffectSlot.STATIC, new RemoveKeywordEffect(Keyword.FLYING, GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.STATIC, new RemoveKeywordEffect(Keyword.ISLANDWALK, GrantScope.ALL_CREATURES));
    }
}
