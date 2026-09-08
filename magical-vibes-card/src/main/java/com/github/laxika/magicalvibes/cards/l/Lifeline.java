package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;

@CardRegistration(set = "USG", collectorNumber = "299")
public class Lifeline extends Card {

    public Lifeline() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, true));
    }
}
