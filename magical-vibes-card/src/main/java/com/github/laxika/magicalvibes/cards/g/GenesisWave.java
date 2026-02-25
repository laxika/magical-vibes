package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GenesisWaveEffect;

@CardRegistration(set = "SOM", collectorNumber = "122")
public class GenesisWave extends Card {

    public GenesisWave() {
        addEffect(EffectSlot.SPELL, new GenesisWaveEffect());
    }
}
