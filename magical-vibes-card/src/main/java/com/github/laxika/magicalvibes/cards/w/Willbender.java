package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TSB", collectorNumber = "36")
public class Willbender extends Card {

    public Willbender() {
        addMorph("{1}{U}");
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryHasTargetPredicate(),
                        new StackEntryIsSingleTargetPredicate()
                )),
                "Target must be a spell or ability with a single target."
        )).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
