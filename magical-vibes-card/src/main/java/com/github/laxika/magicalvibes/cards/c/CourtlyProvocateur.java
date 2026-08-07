package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "46")
public class CourtlyProvocateur extends Card {

    public CourtlyProvocateur() {
        // {T}: Target creature attacks this turn if able.
        // MUST_ATTACK = may attack any legal target, not necessarily the controller.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK)),
                "{T}: Target creature attacks this turn if able.",
                TargetFilters.creature()
        ));

        // {T}: Target creature blocks this turn if able.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK)),
                "{T}: Target creature blocks this turn if able.",
                TargetFilters.creature()
        ));
    }
}
