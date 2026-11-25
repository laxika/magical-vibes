package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "47")
@CardRegistration(set = "EMA", collectorNumber = "30")
public class Soulcatcher extends Card {

    public Soulcatcher() {
        CardEffect flyingCreatureDies = PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE);
        addTrigger(Triggers.creatureDies(new PermanentHasKeywordPredicate(Keyword.FLYING), ObjectRelation.ANY),
                flyingCreatureDies);
    }
}
