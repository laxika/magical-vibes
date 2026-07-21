package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "166")
public class SunsetPyramid extends Card {

    public SunsetPyramid() {
        // This artifact enters with three brick counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.BRICK, new Fixed(3)));

        // {2}, {T}, Remove a brick counter from this artifact: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.BRICK),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Remove a brick counter from this artifact: Draw a card."
        ));

        // {2}, {T}: Scry 1.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ScryEffect(1)),
                "{2}, {T}: Scry 1."
        ));
    }
}
