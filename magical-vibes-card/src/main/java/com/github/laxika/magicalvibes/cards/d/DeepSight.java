package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

/** Deep Sight, the prepare spell of Tam, Observant Sequencer // Deep Sight (SOS 237). */
public class DeepSight extends Card {

    public DeepSight() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
    }
}
