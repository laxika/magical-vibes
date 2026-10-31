package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "189")
public class EarthRift extends Card {

    public EarthRift() {
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect());
        addCastingOption(new FlashbackCast("{5}{R}{R}"));
    }
}
