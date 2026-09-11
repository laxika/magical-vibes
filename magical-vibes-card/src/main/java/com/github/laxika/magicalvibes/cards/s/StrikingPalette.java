package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

/**
 * Striking Palette, the prepare spell of Pigment Wrangler // Striking Palette (SOS 126).
 */
public class StrikingPalette extends Card {

    public StrikingPalette() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect());
    }
}
