package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;

public class Fear extends Card {

    public Fear() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.FEAR));
    }
}
