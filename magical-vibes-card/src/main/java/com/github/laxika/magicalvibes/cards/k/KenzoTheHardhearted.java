package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

public class KenzoTheHardhearted extends Card {

    public KenzoTheHardhearted() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(2));
    }
}
