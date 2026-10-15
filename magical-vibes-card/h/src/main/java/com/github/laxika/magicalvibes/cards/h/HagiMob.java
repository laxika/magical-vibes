package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "140")
public class HagiMob extends Card {

    public HagiMob() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "Boast — {1}{R}: This creature deals 1 damage to any target. Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
