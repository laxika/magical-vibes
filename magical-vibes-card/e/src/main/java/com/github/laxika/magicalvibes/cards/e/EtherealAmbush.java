package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;

@CardRegistration(set = "FRF", collectorNumber = "152")
public class EtherealAmbush extends Card {

    public EtherealAmbush() {
        addSpellEffect(new ManifestTopCardEffect());
        addSpellEffect(new ManifestTopCardEffect());
    }
}
