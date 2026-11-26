package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastAmountPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "13")
@CardRegistration(set = "TDC", collectorNumber = "115")
@CardRegistration(set = "WOE", collectorNumber = "324")
@CardRegistration(set = "WOE", collectorNumber = "381")
public class ExpelTheInterlopers extends Card {

    public ExpelTheInterlopers() {
        addSpellEffect(new ChooseNumberAtResolutionEffect(0, 10));
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastAmountPredicate(new XValue())))));
    }
}
