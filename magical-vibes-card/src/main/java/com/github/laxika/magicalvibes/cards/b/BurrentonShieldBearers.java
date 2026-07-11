package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "MOR", collectorNumber = "4")
public class BurrentonShieldBearers extends Card {

    public BurrentonShieldBearers() {
        // Whenever this creature attacks, target creature gets +0/+3 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(0, 3));
    }
}
