package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "22")
public class UrsineFylgja extends Card {

    public UrsineFylgja() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.HEALING, new Fixed(4)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.HEALING),
                        PreventDamageEffect.nextToSelf(1)
                ),
                "Remove a healing counter from this creature: Prevent the next 1 damage that would be dealt to this creature this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.HEALING)),
                "{2}{W}: Put a healing counter on this creature."
        ));
    }
}
