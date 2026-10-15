package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "125")
public class TemporalSpring extends Card {

    public TemporalSpring() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
