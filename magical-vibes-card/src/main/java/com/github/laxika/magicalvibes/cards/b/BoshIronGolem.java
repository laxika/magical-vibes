package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "147")
public class BoshIronGolem extends Card {

    public BoshIronGolem() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "an artifact", false, false, true, false),
                        new DealDamageToAnyTargetEffect(new XValue())),
                "{3}{R}, Sacrifice an artifact: Bosh, Iron Golem deals damage equal to the sacrificed artifact's mana value to any target."
        ));
    }
}
