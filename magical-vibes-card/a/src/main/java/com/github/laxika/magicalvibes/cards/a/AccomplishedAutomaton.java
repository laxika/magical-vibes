package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "191")
@CardRegistration(set = "2XM", collectorNumber = "230")
public class AccomplishedAutomaton extends Card {

    public AccomplishedAutomaton() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                "Put a +1/+1 counter on Accomplished Automaton?",
                new CreateTokenEffect(1, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))));
    }
}
