package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "NEM", collectorNumber = "106")
public class Mossdog extends Card {

    public Mossdog() {
        // Whenever this creature becomes the target of a spell or ability an opponent controls,
        // put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
