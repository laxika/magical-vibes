package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "13")
public class ScourFromExistence extends Card {

    public ScourFromExistence() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
