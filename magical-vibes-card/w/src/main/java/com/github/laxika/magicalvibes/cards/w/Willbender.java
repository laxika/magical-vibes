package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSB", collectorNumber = "36")
@CardRegistration(set = "LGN", collectorNumber = "58")
@CardRegistration(set = "DD2", collectorNumber = "6")
@CardRegistration(set = "JVC", collectorNumber = "6")
@CardRegistration(set = "DDN", collectorNumber = "47")
@CardRegistration(set = "A25", collectorNumber = "78")
@CardRegistration(set = "C14", collectorNumber = "131")
@CardRegistration(set = "C19", collectorNumber = "102")
public class Willbender extends Card {

    public Willbender() {
        addMorph("{1}{U}");
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryHasTargetPredicate(),
                        new StackEntryIsSingleTargetPredicate()
                )),
                "Target must be a spell or ability with a single target."
        )).addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
