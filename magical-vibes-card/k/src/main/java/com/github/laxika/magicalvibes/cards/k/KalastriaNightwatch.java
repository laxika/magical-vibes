package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "115")
public class KalastriaNightwatch extends Card {

    public KalastriaNightwatch() {
        addTrigger(Triggers.youGainLife(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
