package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "26")
@CardRegistration(set = "TDC", collectorNumber = "66")
public class BoneDevourer extends Card {

    public BoneDevourer() {
        addStaticEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));

        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                new LoseLifeEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        LoseLifeRecipient.CONTROLLER)));
    }
}
