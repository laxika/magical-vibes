package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;

@CardRegistration(set = "RTR", collectorNumber = "67")
public class GraveBetrayal extends Card {

    public GraveBetrayal() {
        // "Whenever a creature you don't control dies, return it to the battlefield under your
        // control with an additional +1/+1 counter on it at the beginning of the next end step.
        // That creature is a black Zombie in addition to its other colors and types."
        // No control-loss sacrifice link — that rider is Seraph's only.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, CounterType.PLUS_ONE_PLUS_ONE, 1, CardColor.BLACK, CardSubtype.ZOMBIE));
    }
}
