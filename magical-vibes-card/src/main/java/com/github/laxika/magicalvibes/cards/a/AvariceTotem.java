package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "104")
public class AvariceTotem extends Card {

    public AvariceTotem() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new ExchangeControlOfTargetPermanentsEffect(
                        TargetFilters.nonlandPermanent().predicate(), false, false, true)),
                "{5}: Exchange control of this artifact and target nonland permanent.",
                TargetFilters.nonlandPermanent()
        ));
    }
}
