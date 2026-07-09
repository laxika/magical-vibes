package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "9ED", collectorNumber = "38")
public class SacredNectar extends Card {

    public SacredNectar() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
