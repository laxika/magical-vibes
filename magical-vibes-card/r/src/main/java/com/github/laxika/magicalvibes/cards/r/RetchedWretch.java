package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ECL", collectorNumber = "117")
public class RetchedWretch extends Card {

    public RetchedWretch() {
        addTrigger(Triggers.dies(
                        new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                        ObjectRelation.THIS),
                new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard())
                .enteringWith(new LosesAllAbilitiesEffect(Subjects.target(), Duration.PERMANENT)));
    }
}
