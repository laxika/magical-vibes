package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M15", collectorNumber = "48")
public class Chronostutter extends Card {

    public Chronostutter() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopEffect(1));
    }
}
