package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "POR", collectorNumber = "157")
@CardRegistration(set = "P02", collectorNumber = "121")
@CardRegistration(set = "S99", collectorNumber = "124")
public class AlluringScent extends Card {

    public AlluringScent() {
        // All creatures able to block target creature this turn do so.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED_BY_ALL));
    }
}
