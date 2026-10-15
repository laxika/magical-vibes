package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "290")
@CardRegistration(set = "TSB", collectorNumber = "107")
public class ClawsOfGix extends Card {

    public ClawsOfGix() {
        // {1}, Sacrifice a permanent: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentTruePredicate(), "Sacrifice a permanent", false),
                        new GainLifeEffect(1)
                ),
                "{1}, Sacrifice a permanent: You gain 1 life."
        ));
    }
}
