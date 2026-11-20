package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "691")
public class IntrepidAce extends Card {

    public IntrepidAce() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.source(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsAttackingPredicate()),
                        new PermanentNotPredicate(new PermanentIsBlockingPredicate())))));
    }
}
