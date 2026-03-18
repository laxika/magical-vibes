package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "181")
public class EntanglingVines extends Card {

    public EntanglingVines() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                )),
                "Target must be a tapped creature"
        )).addEffect(EffectSlot.STATIC, new AttachedCreatureDoesntUntapEffect());
    }
}
