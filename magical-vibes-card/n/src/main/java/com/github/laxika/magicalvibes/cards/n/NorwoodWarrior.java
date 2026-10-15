package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "P02", collectorNumber = "140")
public class NorwoodWarrior extends Card {

    public NorwoodWarrior() {
        // Whenever this creature becomes blocked, it gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));
    }
}
