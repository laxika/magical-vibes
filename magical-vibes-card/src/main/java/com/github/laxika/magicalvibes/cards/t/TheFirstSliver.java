package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "MH1", collectorNumber = "200")
public class TheFirstSliver extends Card {

    public TheFirstSliver() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addEffect(EffectSlot.GRANT_CASCADE_TO_SLIVER_SPELL, new CascadeEffect());
    }
}
