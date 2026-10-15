package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;

@CardRegistration(set = "DSK", collectorNumber = "38")
public class UnsettlingTwins extends Card {

    public UnsettlingTwins() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ManifestDreadEffect.forController());
    }
}
