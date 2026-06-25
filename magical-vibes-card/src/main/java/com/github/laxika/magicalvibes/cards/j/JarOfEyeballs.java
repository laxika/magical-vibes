package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "152")
public class JarOfEyeballs extends Card {

    public JarOfEyeballs() {
        // Whenever a creature you control dies, put two eyeball counters on Jar of Eyeballs.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new PutCountersOnSelfEffect(CounterType.EYEBALL, 2));

        // {3}, {T}, Remove all eyeball counters from Jar of Eyeballs: Look at the top X cards of
        // your library, where X is the number of eyeball counters removed this way. Put one of
        // them into your hand and the rest on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new RemoveAllCountersAsCostEffect(CounterType.EYEBALL),
                        new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect()),
                "{3}, {T}, Remove all eyeball counters from Jar of Eyeballs: Look at the top X cards of your library, where X is the number of eyeball counters removed this way. Put one of them into your hand and the rest on the bottom of your library in any order."
        ));
    }
}
