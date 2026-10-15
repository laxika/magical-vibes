package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "30")
@CardRegistration(set = "DMC", collectorNumber = "52")
public class TheEverChangingDane extends Card {

    public TheEverChangingDane() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        SacrificePermanentCost.withPermanentSnapshot(
                                new PermanentIsCreaturePredicate(), "another creature"),
                        new BecomeCopyOfSacrificedPermanentEffect()),
                "{1}, Sacrifice another creature: The Ever-Changing 'Dane becomes a copy of the sacrificed creature, except it has this ability."
        ));
    }
}
