package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttackingPlayerChoosesBlockersEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "STH", collectorNumber = "89")
public class InvasionPlans extends Card {

    public InvasionPlans() {
        addStaticEffect(new MustBlockEachCombatEffect(Subjects.allCreatures()));
        addStaticEffect(new AttackingPlayerChoosesBlockersEffect());
    }
}
