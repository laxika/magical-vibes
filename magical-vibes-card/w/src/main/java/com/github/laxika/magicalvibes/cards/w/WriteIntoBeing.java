package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManifestOneOfTopTwoEffect;

@CardRegistration(set = "FRF", collectorNumber = "59")
public class WriteIntoBeing extends Card {

    public WriteIntoBeing() {
        addSpellEffect(new ManifestOneOfTopTwoEffect());
    }
}
