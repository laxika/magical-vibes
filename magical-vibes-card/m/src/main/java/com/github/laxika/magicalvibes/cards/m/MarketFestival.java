package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "130")
public class MarketFestival extends Card {

    public MarketFestival() {
        target(TargetFilters.land())
                .addTrigger(Triggers.playerTapsLandForMana(),
                        new AddManaOnEnchantedLandTapEffect(
                                new AwardAnyColorManaEffect(2, true)));
    }
}
