package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;

public class SerrasEmbrace extends Card {

    public SerrasEmbrace() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureEffect(2, 2));
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.FLYING));
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.VIGILANCE));
    }
}
