package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "FRF", collectorNumber = "72")
public class GurmagAngler extends Card {

    public GurmagAngler() {
        addEffect(EffectSlot.SPELL, new DelveCost());
    }
}
