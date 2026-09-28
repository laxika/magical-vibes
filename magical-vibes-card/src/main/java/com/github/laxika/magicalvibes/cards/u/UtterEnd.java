package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "210")
@CardRegistration(set = "MOC", collectorNumber = "341")
@CardRegistration(set = "C21", collectorNumber = "232")
@CardRegistration(set = "40K", collectorNumber = "226")
@CardRegistration(set = "DSC", collectorNumber = "91")
@CardRegistration(set = "LCC", collectorNumber = "292")
public class UtterEnd extends Card {

    public UtterEnd() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
