package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "111")
public class RobotDomination extends Card {

    public RobotDomination() {
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                ObjectRelation.ANOTHER_YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new LoseLifeEffect(1),
                        new PutCountersOnSelfEffect(CounterType.PLAN)));

        addEffect(EffectSlot.ON_SELF_COUNTERS_PUT, ConditionalEffect.atTriggerTime(
                new SourceCounterThreshold(3, CounterType.PLAN),
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new CreateTokenEffect(
                                3, "Robot", 2, 2, null,
                                List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN), Set.of(),
                                Set.of(CardType.ARTIFACT)))));
    }
}
