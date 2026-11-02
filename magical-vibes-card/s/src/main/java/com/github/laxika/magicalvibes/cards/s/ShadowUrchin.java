package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "242")
@CardRegistration(set = "ECL", collectorNumber = "379")
public class ShadowUrchin extends Card {

    public ShadowUrchin() {
        addTrigger(Triggers.attacks(), new BlightEffect(1, null));
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.ANY),
                new ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect()));
    }
}
