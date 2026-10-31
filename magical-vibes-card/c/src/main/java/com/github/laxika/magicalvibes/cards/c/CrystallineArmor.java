package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "137")
@CardRegistration(set = "TLE", collectorNumber = "204")
public class CrystallineArmor extends Card {

    public CrystallineArmor() {
        target(TargetFilters.creature());

        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new DynamicStaticBoostEffect(
                landsYouControl, landsYouControl, GrantScope.ENCHANTED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE));
    }
}
