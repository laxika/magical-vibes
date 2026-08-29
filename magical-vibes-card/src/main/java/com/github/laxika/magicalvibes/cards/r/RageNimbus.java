package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "160")
public class RageNimbus extends Card {

    public RageNimbus() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK)),
                "{1}{R}: Target creature attacks this turn if able.",
                TargetFilters.creature()
        ));
    }
}
