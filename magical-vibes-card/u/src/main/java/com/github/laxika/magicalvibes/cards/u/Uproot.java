package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BOK", collectorNumber = "149")
public class Uproot extends Card {

    public Uproot() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
