package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

@CardRegistration(set = "DGM", collectorNumber = "10")
public class WakeTheReflections extends Card {

    public WakeTheReflections() {
        // Populate.
        addEffect(EffectSlot.SPELL, new PopulateEffect());
    }
}
