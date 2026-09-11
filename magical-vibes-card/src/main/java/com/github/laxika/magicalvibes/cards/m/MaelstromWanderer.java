package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "PC2", collectorNumber = "101")
public class MaelstromWanderer extends Card {

    public MaelstromWanderer() {
        // Creatures you control have haste, including Maelstrom Wanderer.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));

        // Maelstrom Wanderer has cascade twice.
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
