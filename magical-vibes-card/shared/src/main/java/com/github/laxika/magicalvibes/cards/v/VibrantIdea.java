package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

/**
 * Vibrant Idea, the prepare spell of Landscape Painter // Vibrant Idea (SOS 56).
 */
public class VibrantIdea extends Card {

    public VibrantIdea() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
