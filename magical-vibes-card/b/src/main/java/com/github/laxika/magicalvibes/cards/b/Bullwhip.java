package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "132")
public class Bullwhip extends Card {

    public Bullwhip() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DealDamageToTargetCreatureEffect(1),
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK)),
                "{2}, {T}: This artifact deals 1 damage to target creature. That creature attacks this turn if able.",
                TargetFilters.creature()
        ));
    }
}
