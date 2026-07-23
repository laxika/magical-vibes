package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerPowerDamageEffect;

@CardRegistration(set = "ICE", collectorNumber = "128")
public class GazeOfPain extends Card {

    public GazeOfPain() {
        // Until end of turn, whenever a creature you control attacks and isn't blocked, you may
        // choose to have it deal damage equal to its power to a target creature. If you do, it
        // assigns no combat damage this turn.
        addEffect(EffectSlot.SPELL, new RegisterDelayedUnblockedAttackerPowerDamageEffect());
    }
}
