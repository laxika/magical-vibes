package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "M10", collectorNumber = "48")
public class Disorient extends Card {

    public Disorient() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-7, 0));
    }
}
