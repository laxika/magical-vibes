package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AA3", collectorNumber = "11")
@CardRegistration(set = "SLD", collectorNumber = "2410")
public class BroadsideBombardiers extends Card {

    public BroadsideBombardiers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsArtifactPredicate())),
                                "another creature or artifact",
                                true,
                                false,
                                true,
                                false),
                        new DealDamageToAnyTargetEffect(new Sum(new Fixed(2), new XValue()))
                ),
                "Boast — Sacrifice another creature or artifact: This creature deals damage equal to 2 plus "
                        + "the sacrificed permanent's mana value to any target. Activate only if this creature "
                        + "attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
