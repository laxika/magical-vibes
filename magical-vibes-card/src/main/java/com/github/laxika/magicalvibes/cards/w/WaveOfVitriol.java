package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WaveOfVitriolEffect;

@CardRegistration(set = "C14", collectorNumber = "51")
public class WaveOfVitriol extends Card {

    public WaveOfVitriol() {
        // Each player sacrifices all artifacts, enchantments, and nonbasic lands they control.
        // For each land sacrificed this way, its controller may search for a basic land and put it
        // onto the battlefield tapped.
        addEffect(EffectSlot.SPELL, new WaveOfVitriolEffect());
    }
}
