package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToAmountUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "120")
public class ArniBrokenbrow extends Card {

    public ArniBrokenbrow() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new MayEffect(
                        new SetSelfBasePowerToAmountUntilEndOfTurnEffect(new Sum(
                                new Fixed(1),
                                GreatestPowerAmongControlled.includingNegative(
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                        "Change Arni's base power?")),
                "Boast — {1}: You may change Arni's base power to 1 plus the greatest power among other creatures you control until end of turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
