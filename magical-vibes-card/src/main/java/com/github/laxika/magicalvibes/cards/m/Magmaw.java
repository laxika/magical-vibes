package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "158")
public class Magmaw extends Card {

    public Magmaw() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                "a nonland permanent",
                                false
                        ),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "{1}, Sacrifice a nonland permanent: This creature deals 1 damage to any target."
        ));
    }
}
