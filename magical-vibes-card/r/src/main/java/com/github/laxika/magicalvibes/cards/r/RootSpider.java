package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "HML", collectorNumber = "94")
public class RootSpider extends Card {

    public RootSpider() {
        // Whenever this creature blocks, it gets +1/+0 and gains first strike until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(1, 0));
        addEffect(EffectSlot.ON_BLOCK, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
    }
}
