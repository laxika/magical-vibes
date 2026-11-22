package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "223")
public class GrakmawSkyclaveRavager extends Card {

    public GrakmawSkyclaveRavager() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        addTrigger(Triggers.creatureDies(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        var counters = new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE);
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE, new Fixed(1), "Hydra", counters, counters,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.HYDRA),
                Set.of(), Set.of(), false, false, List.of(), List.of(), false, false, false, 0, Set.of()));
    }
}
