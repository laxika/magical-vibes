package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "452")
@CardRegistration(set = "FIN", collectorNumber = "558")
public class SeymourFlux extends Card {

    public SeymourFlux() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayPayLifeEffect(
                1,
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "Pay 1 life to draw a card and put a +1/+1 counter on Seymour Flux?"));
    }
}
