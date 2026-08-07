package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WTH", collectorNumber = "92")
public class BoilingBlood extends Card {

    public BoilingBlood() {
        target(TargetFilters.creature())
          .addEffect(EffectSlot.SPELL, new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK))
          .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
