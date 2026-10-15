package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect;

@CardRegistration(set = "ROE", collectorNumber = "84")
public class RenegadeDoppelganger extends Card {

    public RenegadeDoppelganger() {
        // Whenever another creature you control enters, you may have this creature become a copy
        // of that creature until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect());
    }
}
