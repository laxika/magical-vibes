package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "127")
public class BarrageTyrant extends Card {

    public BarrageTyrant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsColorlessPredicate(),
                                        new PermanentIsCreaturePredicate()
                                )),
                                "another colorless creature",
                                true,
                                true
                        ),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "{2}{R}, Sacrifice another colorless creature: This creature deals damage equal to the sacrificed creature's power to any target."
        ));
    }
}
