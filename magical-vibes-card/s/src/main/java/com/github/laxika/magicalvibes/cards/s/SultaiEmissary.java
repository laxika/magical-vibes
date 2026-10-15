package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;

@CardRegistration(set = "FRF", collectorNumber = "85")
public class SultaiEmissary extends Card {

    public SultaiEmissary() {
        addEffect(EffectSlot.ON_DEATH, new ManifestTopCardEffect());
    }
}
