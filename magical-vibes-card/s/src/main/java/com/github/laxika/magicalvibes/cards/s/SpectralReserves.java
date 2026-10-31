package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "EMN", collectorNumber = "43")
public class SpectralReserves extends Card {

    public SpectralReserves() {
        addSpellEffect(CreateTokenEffect.whiteSpirit(2));
        addSpellEffect(new GainLifeEffect(2));
    }
}
