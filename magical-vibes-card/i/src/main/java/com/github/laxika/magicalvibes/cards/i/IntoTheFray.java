package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOK", collectorNumber = "106")
public class IntoTheFray extends Card {

    public IntoTheFray() {
        target(TargetFilters.creature())
                .addSpellEffect(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK));
        addStaticEffect(new SpliceEffect(CardSubtype.ARCANE, "{R}"));
    }
}
