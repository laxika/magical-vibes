package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;

@CardRegistration(set = "MOR", collectorNumber = "100")
public class RoarOfTheCrowd extends Card {

    public RoarOfTheCrowd() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new DealDamageToAnyTargetEffect(new PermanentCount(new PermanentHasTypeChosenThisResolutionPredicate(), CountScope.CONTROLLER)));
    }
}
