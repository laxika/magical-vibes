package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "342")
@CardRegistration(set = "7ED", collectorNumber = "282")
@CardRegistration(set = "6ED", collectorNumber = "268")
@CardRegistration(set = "4ED", collectorNumber = "289")
@CardRegistration(set = "ICE", collectorNumber = "277")
@CardRegistration(set = "BTD", collectorNumber = "64")
public class WildGrowth extends Card {

    public WildGrowth() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 1)));
    }
}
