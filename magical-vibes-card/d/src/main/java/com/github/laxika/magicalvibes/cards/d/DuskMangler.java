package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureOrDiscardCardOrPayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "77")
public class DuskMangler extends Card {

    public DuskMangler() {
        addCastingCost(new SacrificeCreatureOrDiscardCardOrPayLifeCost(4));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificePermanentsEffect(
                        1, new PermanentIsCreaturePredicate(), Subjects.eachOpponent()),
                new DiscardEffect(1, Subjects.eachOpponent()),
                new LoseLifeEffect(4, Subjects.eachOpponent()));
    }
}
