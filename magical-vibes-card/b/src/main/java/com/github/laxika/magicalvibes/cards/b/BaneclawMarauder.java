package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class BaneclawMarauder extends Card {

    public BaneclawMarauder() {
        addTrigger(Triggers.becomesBlocked(),
                BoostEffect.allCreatures(-1, -1, new PermanentBlockingSourcePredicate()));
        addTrigger(Triggers.creatureDies(new PermanentBlockingSourcePredicate(), ObjectRelation.ANY),
                new LoseLifeEffect(1, Subjects.controllerOf(Subjects.triggering())));
    }
}
