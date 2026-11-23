package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "131")
@CardRegistration(set = "C13", collectorNumber = "102")
public class CapriciousEfreet extends Card {

    public CapriciousEfreet() {
        var nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        target(new ControlledPermanentPredicateTargetFilter(nonland, "Choose a nonland permanent you control."));
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(nonland,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Choose a nonland permanent you don't control."), 0, 2)
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                        ChoosePermanentsEffect.atRandom(1, Subjects.allTargets(), "one of them"),
                        new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE))));
    }
}
