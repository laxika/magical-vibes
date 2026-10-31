package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M11", collectorNumber = "145")
public class Incite extends Card {

    public Incite() {
        target(TargetFilters.creature()).addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.RED))
          .addSpellEffect(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK));
    }
}
