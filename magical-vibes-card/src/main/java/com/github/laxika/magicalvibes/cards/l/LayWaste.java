package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "201")
public class LayWaste extends Card {

    public LayWaste() {
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addCycling("{2}");
    }
}
