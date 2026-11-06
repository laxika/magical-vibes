package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;

@CardRegistration(set = "KTK", collectorNumber = "136")
@CardRegistration(set = "MKC", collectorNumber = "171")
@CardRegistration(set = "C19", collectorNumber = "172")
public class HoodedHydra extends Card {

    public HoodedHydra() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SNAKE), Set.of(), Set.of())
                                .withAmount(new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)));
        addMorph("{3}{G}{G}");
        addStaticEffect(new PutCountersOnTurnFaceUpEffect(5));
    }
}
