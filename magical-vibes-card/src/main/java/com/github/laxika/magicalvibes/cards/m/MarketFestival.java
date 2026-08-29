package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "130")
public class MarketFestival extends Card {

    public MarketFestival() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                        new AddManaOnEnchantedLandTapEffect(
                                new AwardAnyColorManaEffect(2, true)));
    }
}
