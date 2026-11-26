package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOR", collectorNumber = "73")
public class PacksDisdain extends Card {

    public PacksDisdain() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        DynamicAmount minusOnePerPermanent = new Scaled(
                new PermanentCount(new PermanentHasTypeChosenThisResolutionPredicate(), CountScope.CONTROLLER), -1);
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(minusOnePerPermanent, minusOnePerPermanent));
    }
}
