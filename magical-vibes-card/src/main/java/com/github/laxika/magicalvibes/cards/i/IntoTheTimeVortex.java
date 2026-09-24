package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "MSC", collectorNumber = "165")
@CardRegistration(set = "MSC", collectorNumber = "363")
public class IntoTheTimeVortex extends Card {

    public IntoTheTimeVortex() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
