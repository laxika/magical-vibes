package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "2")
public class BattershieldWarrior extends Card {

    public BattershieldWarrior() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "Boast — {1}{W}: Creatures you control get +1/+1 until end of turn. "
                        + "Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
