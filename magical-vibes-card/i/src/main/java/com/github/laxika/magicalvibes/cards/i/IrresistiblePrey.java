package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "188")
public class IrresistiblePrey extends Card {

    public IrresistiblePrey() {
        target(TargetFilters.creature())
                .addSpellEffect(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
