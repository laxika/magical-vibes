package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "USG", collectorNumber = "321")
public class GaeasCradle extends Card {

    public GaeasCradle() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(
                ManaColor.GREEN,
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)
        ));
    }
}
