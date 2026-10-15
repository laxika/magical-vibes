package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "33")
public class SanctumOfTranquilLight extends Card {

    public SanctumOfTranquilLight() {
        PermanentCount shrinesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}",
                List.of(
                        new ReduceActivationCostEffect(shrinesYouControl),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{5}{W}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
