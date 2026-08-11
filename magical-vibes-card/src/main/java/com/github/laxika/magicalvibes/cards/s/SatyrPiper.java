package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "175")
public class SatyrPiper extends Card {

    public SatyrPiper() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED)),
                "{3}{G}: Target creature must be blocked this turn if able.",
                TargetFilters.creature()
        ));
    }
}
