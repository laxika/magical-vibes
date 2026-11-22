package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastToughnessAmongOpponentCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "7")
public class PenregonBesieged extends Card {

    public PenregonBesieged() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                ChoosePermanentsEffect.exactly(1,
                        Subjects.controlledBy(Subjects.eachOpponent(), new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasLeastToughnessAmongOpponentCreaturesPredicate()))),
                        "a creature with the least toughness among creatures your opponents control"),
                new PerpetuallyBoostTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), -1, -1));

        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentIsCreaturePredicate()))),
                new SacrificeSelfEffect());
    }
}
