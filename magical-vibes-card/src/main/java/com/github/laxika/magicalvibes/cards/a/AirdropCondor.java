package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "186")
public class AirdropCondor extends Card {

    public AirdropCondor() {
        // {1}{R}, Sacrifice a Goblin creature: This creature deals damage equal to the sacrificed
        // creature's power to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                                )),
                                "Sacrifice a Goblin creature",
                                false,
                                true
                        ),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "{1}{R}, Sacrifice a Goblin creature: This creature deals damage equal to the "
                        + "sacrificed creature's power to any target."
        ));
    }
}
