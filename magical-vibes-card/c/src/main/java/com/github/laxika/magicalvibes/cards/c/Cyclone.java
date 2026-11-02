package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PayPerCounterOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "62")
@CardRegistration(set = "ME4", collectorNumber = "148")
@CardRegistration(set = "ARN", collectorNumber = "45")
public class Cyclone extends Card {

    public Cyclone() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.WIND),
                new PayPerCounterOrElseEffect(
                        CounterType.WIND,
                        "{G}",
                        List.of(new SacrificeSelfEffect()),
                        List.of(new MassDamageEffect(new CountersOnSource(CounterType.WIND), true)))));
    }
}
