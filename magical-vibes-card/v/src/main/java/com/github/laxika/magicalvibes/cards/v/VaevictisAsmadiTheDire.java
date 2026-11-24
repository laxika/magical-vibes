package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "225")
@CardRegistration(set = "SLD", collectorNumber = "1812")
@CardRegistration(set = "SLD", collectorNumber = "2239")
public class VaevictisAsmadiTheDire extends Card {

    public VaevictisAsmadiTheDire() {
        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_PLAYER);
        // A target that isn't sacrificed (it became illegal) has its controller reveal nothing (the card's ruling).
        target(TargetFilters.permanent(), 0, 99)
                .addTrigger(Triggers.attacks(),
                        new SacrificeSelfEffect(Subjects.allTargets(), Subjects.controllerOf(Subjects.allTargets()),
                                new PermanentTruePredicate()),
                        new RevealTopCardPermanentToBattlefieldEffect(Subjects.playersThisWay(StepKind.SACRIFICE)));
    }
}
