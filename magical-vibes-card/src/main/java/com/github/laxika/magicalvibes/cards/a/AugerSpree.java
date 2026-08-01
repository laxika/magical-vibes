package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "RTR", collectorNumber = "144")
public class AugerSpree extends Card {

    public AugerSpree() {
        // Target creature gets +4/-4 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, -4));
    }
}
