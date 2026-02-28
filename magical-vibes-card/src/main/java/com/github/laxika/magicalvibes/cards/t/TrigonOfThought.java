package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "217")
public class TrigonOfThought extends Card {

    public TrigonOfThought() {
        // Trigon of Thought enters the battlefield with three charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        // {U}{U}, {T}: Put a charge counter on Trigon of Thought.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(new PutChargeCounterOnSelfEffect()),
                "{U}{U}, {T}: Put a charge counter on Trigon of Thought."
        ));

        // {2}, {T}, Remove a charge counter from Trigon of Thought: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Remove a charge counter from Trigon of Thought: Draw a card."
        ));
    }
}
