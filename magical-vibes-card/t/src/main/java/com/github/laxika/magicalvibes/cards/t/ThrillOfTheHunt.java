package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "229")
@CardRegistration(set = "TSR", collectorNumber = "241")
public class ThrillOfTheHunt extends Card {

    public ThrillOfTheHunt() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 2));
        addCastingOption(new FlashbackCast("{W}"));
    }
}
