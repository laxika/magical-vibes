package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "EMN", collectorNumber = "43")
public class SpectralReserves extends Card {

    public SpectralReserves() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
