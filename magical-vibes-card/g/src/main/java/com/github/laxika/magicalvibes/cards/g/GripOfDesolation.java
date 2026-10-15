package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "94")
public class GripOfDesolation extends Card {

    public GripOfDesolation() {
        setAllowSharedTargets(true);

        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
