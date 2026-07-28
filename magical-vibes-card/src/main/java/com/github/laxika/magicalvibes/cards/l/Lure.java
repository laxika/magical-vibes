package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "314")
@CardRegistration(set = "4ED", collectorNumber = "262")
@CardRegistration(set = "10E", collectorNumber = "276")
@CardRegistration(set = "8ED", collectorNumber = "263")
@CardRegistration(set = "7ED", collectorNumber = "255")
@CardRegistration(set = "6ED", collectorNumber = "240")
@CardRegistration(set = "ICE", collectorNumber = "253")
public class Lure extends Card {

    public Lure() {
        // Enchant creature
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
    }
}
