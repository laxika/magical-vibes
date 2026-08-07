package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "286")
public class EssenceBottle extends Card {

    public EssenceBottle() {
        // {3}, {T}: Put an elixir counter on Essence Bottle.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new PutCountersOnSelfEffect(CounterType.ELIXIR, 1)),
                "{3}, {T}: Put an elixir counter on Essence Bottle."
        ));

        // {T}, Remove all elixir counters from Essence Bottle: You gain 2 life for each elixir
        // counter removed this way. The removed count is snapshotted into the ability's X.
        addActivatedAbility(new ActivatedAbility(
                true, "{0}",
                List.of(new RemoveAllCountersAsCostEffect(CounterType.ELIXIR),
                        new GainLifeEffect(new Scaled(new XValue(), 2))),
                "{T}, Remove all elixir counters from Essence Bottle: You gain 2 life for each elixir counter removed this way."
        ));
    }
}
