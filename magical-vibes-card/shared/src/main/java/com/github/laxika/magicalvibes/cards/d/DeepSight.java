package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

/** Deep Sight, the prepare spell of Tam, Observant Sequencer // Deep Sight (SOS 237). */
public class DeepSight extends Card {

    public DeepSight() {
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new GainLifeEffect(1));
    }
}
