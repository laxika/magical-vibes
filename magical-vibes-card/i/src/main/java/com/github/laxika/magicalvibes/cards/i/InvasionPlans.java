package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllCreaturesMustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.AttackingPlayerChoosesBlockersEffect;

@CardRegistration(set = "STH", collectorNumber = "89")
public class InvasionPlans extends Card {

    public InvasionPlans() {
        addStaticEffect(new AllCreaturesMustBlockEachCombatEffect());
        addStaticEffect(new AttackingPlayerChoosesBlockersEffect());
    }
}
