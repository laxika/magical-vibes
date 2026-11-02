package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "118")
public class NirkanaAssassin extends Card {

    public NirkanaAssassin() {
        addTrigger(Triggers.youGainLife(),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF));
    }
}
