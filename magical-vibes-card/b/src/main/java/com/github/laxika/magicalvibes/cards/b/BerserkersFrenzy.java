package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.RollTwoD20IgnoreLowerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AFC", collectorNumber = "29")
public class BerserkersFrenzy extends Card {

    public BerserkersFrenzy() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_BLOCKERS_DECLARED);
        addSpellEffect(new RollTwoD20IgnoreLowerEffect(
                SequenceEffect.of(
                        ChoosePermanentsEffect.anyNumber(Subjects.allCreatures(), "any number of creatures"),
                        new SetCombatRequirementThisTurnEffect(
                                CombatRequirement.MUST_BLOCK, Subjects.thisWay(StepKind.CHOOSE))),
                new ChooseBlockersThisCombatEffect(true),
                14));
    }
}
