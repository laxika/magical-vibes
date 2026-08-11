package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "116")
public class PhyrexianInfiltrator extends Card {

    public PhyrexianInfiltrator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentIsCreaturePredicate(), false, true, true)),
                "{2}{U}{U}: Exchange control of this creature and target creature.",
                TargetFilters.creature()
        ));
    }
}
