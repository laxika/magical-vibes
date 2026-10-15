package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "SCG", collectorNumber = "38")
public class LongTermPlans extends Card {

    public LongTermPlans() {
        addEffect(EffectSlot.SPELL, SearchLibraryEffect.topOfLibraryAtPosition(null, 2));
    }
}
