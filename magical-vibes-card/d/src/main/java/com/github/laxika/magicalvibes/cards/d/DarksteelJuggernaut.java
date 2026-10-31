package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "SOM", collectorNumber = "150")
@CardRegistration(set = "BRC", collectorNumber = "137")
@CardRegistration(set = "C18", collectorNumber = "202")
@CardRegistration(set = "FDC", collectorNumber = "252")
public class DarksteelJuggernaut extends Card {

    public DarksteelJuggernaut() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(artifactsYouControl, artifactsYouControl));
        addStaticEffect(new MustAttackEffect());
    }
}
