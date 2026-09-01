package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;

@CardRegistration(set = "FRF", collectorNumber = "152")
public class EtherealAmbush extends Card {

    public EtherealAmbush() {
        addEffect(EffectSlot.SPELL, new ManifestTopCardEffect());
        addEffect(EffectSlot.SPELL, new ManifestTopCardEffect());
    }
}
