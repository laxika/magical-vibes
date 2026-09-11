package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;

@CardRegistration(set = "DSK", collectorNumber = "103")
public class InnocuousRat extends Card {

    public InnocuousRat() {
        addEffect(EffectSlot.ON_DEATH, ManifestDreadEffect.forController());
    }
}
