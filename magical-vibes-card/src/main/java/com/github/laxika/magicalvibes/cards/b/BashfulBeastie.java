package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;

@CardRegistration(set = "DSK", collectorNumber = "169")
public class BashfulBeastie extends Card {

    public BashfulBeastie() {
        addEffect(EffectSlot.ON_DEATH, ManifestDreadEffect.forController());
    }
}
