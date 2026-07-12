package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SHM", collectorNumber = "23")
public class SpectralProcession extends Card {

    public SpectralProcession() {
        // Create three 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(3));
    }
}
