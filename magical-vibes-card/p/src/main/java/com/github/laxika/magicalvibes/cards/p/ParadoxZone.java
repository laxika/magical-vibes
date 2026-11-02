package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "64")
public class ParadoxZone extends Card {

    public ParadoxZone() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.GROWTH, new Fixed(1)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.GROWTH,
                        new CountersOnSource(CounterType.GROWTH)),
                new CreateXTokenWithXCountersEffect(
                        "Fractal", 0, 0,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                        List.of(CardSubtype.FRACTAL),
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CountersOnSource(CounterType.GROWTH))));
    }
}
