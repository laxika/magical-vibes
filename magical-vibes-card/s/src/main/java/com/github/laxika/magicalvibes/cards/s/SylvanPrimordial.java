package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "136")
@CardRegistration(set = "PIO", collectorNumber = "197")
public class SylvanPrimordial extends Card {

    public SylvanPrimordial() {
        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_OPPONENT_IF_ABLE);
        target(TargetFilters.noncreaturePermanentAnOpponentControls(), 0, 99)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(Subjects.allTargets()),
                                new SearchLibraryEffect(
                                        new AmountThisWay(StepKind.DESTROY),
                                        new CardSubtypePredicate(CardSubtype.FOREST),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED)));
    }
}
