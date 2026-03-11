package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "M11", collectorNumber = "15")
public class GoldenglowMoth extends Card {

    public GoldenglowMoth() {
        // Flying keyword is auto-loaded from Scryfall.
        // Whenever Goldenglow Moth blocks, you may gain 4 life.
        addEffect(EffectSlot.ON_BLOCK, new MayEffect(new GainLifeEffect(4), "Gain 4 life?"));
    }
}
