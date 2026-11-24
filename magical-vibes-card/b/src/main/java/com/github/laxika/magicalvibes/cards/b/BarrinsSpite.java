package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "235")
public class BarrinsSpite extends Card {

    public BarrinsSpite() {
        // Choose two target creatures controlled by the same player. Their controller chooses and
        // sacrifices one of them. Return the other to its owner's hand.
        target(TargetFilters.creature())
                .addSpellEffect(ChoosePermanentsEffect.exactly(1, Subjects.allTargets(),
                                "a creature to sacrifice; the other returns to its owner's hand")
                        .chosenBy(Subjects.controllerOf(Subjects.allTargets())))
                .addSpellEffect(new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE),
                        Subjects.controllerOf(Subjects.thisWay(StepKind.CHOOSE))))
                .addSpellEffect(new ReturnToHandEffect(
                        Subjects.except(Subjects.allTargets(), Subjects.thisWay(StepKind.CHOOSE))));
        target(TargetFilters.creature());
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
