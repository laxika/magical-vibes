package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;

import java.util.List;

public class ShimmeringWings extends Card {

    public ShimmeringWings() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.FLYING));
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new ReturnSelfToHandEffect()), false, "{U}: Return Shimmering Wings to its owner's hand."));
    }
}
