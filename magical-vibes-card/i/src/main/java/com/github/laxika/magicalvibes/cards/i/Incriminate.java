package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "84")
public class Incriminate extends Card {

    public Incriminate() {
        target(TargetFilters.creature())
                .addSpellEffect(ChoosePermanentsEffect.exactly(1, Subjects.allTargets(), "a creature to sacrifice")
                        .chosenBy(Subjects.controllerOf(Subjects.allTargets())).toSacrifice())
                .addSpellEffect(new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE),
                        Subjects.controllerOf(Subjects.thisWay(StepKind.CHOOSE))));
        target(TargetFilters.creature());
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
