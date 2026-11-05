package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "97")
public class DwarvenBerserker extends Card {

    public DwarvenBerserker() {
        // Whenever this creature becomes blocked, it gets +3/+0 and gains trample until end of turn.
        addTrigger(Triggers.becomesBlocked(),
                new BoostSelfEffect(3, 0),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
