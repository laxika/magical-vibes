package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "120")
public class CullingMark extends Card {

    public CullingMark() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK));
    }
}
