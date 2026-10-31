package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "175")
public class DeclareDominance extends Card {

    public DeclareDominance() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3))
                .addSpellEffect(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED_BY_ALL));
    }
}
