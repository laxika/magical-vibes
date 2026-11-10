package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class KenzoTheHardhearted extends Card {

    public KenzoTheHardhearted() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(2));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(2));
    }
}
