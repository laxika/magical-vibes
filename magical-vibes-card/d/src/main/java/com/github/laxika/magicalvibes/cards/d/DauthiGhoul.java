package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "121")
public class DauthiGhoul extends Card {

    public DauthiGhoul() {
        // Whenever a creature with shadow dies, put a +1/+1 counter on Dauthi Ghoul.
        addTrigger(Triggers.creatureDies(new PermanentHasKeywordPredicate(Keyword.SHADOW), ObjectRelation.ANY),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
