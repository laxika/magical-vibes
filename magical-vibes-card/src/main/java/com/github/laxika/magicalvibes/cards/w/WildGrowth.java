package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "342")
@CardRegistration(set = "7ED", collectorNumber = "282")
@CardRegistration(set = "6ED", collectorNumber = "268")
public class WildGrowth extends Card {

    public WildGrowth() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        ))
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 1)));
    }
}
