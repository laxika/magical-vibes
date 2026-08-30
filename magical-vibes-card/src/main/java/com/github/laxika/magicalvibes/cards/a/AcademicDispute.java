package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "91")
public class AcademicDispute extends Card {

    public AcademicDispute() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK))
                .addEffect(EffectSlot.SPELL, new MayEffect(
                        new GrantKeywordEffect(Keyword.REACH, GrantScope.TARGET),
                        "Have it gain reach until end of turn?"));

        addEffect(EffectSlot.SPELL, new LearnEffect());
    }
}
