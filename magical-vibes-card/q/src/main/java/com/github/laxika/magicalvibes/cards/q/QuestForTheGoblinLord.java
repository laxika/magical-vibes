package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "86")
public class QuestForTheGoblinLord extends Card {

    public QuestForTheGoblinLord() {
        addTrigger(Triggers.enters(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Quest for the Goblin Lord?"));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.QUEST),
                new StaticBoostEffect(2, 0, GrantScope.ALL_OWN_CREATURES)));
    }
}
