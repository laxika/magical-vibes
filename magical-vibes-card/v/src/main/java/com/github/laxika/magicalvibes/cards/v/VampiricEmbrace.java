package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "164")
public class VampiricEmbrace extends Card {

    public VampiricEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.FLYING), Subjects.attachedCreature()))
                .addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_ATTACHED),
                        new PutCounterOnTargetPermanentEffect(Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
