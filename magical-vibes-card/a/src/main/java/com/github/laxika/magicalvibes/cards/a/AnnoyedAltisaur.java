package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "2X2", collectorNumber = "134")
public class AnnoyedAltisaur extends Card {

    public AnnoyedAltisaur() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
