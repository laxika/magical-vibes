package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "54")
@CardRegistration(set = "M19", collectorNumber = "81")
@CardRegistration(set = "WAR", collectorNumber = "74")
public class TotallyLost extends Card {

    public TotallyLost() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
