package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;


@CardRegistration(set = "MSC", collectorNumber = "589")
public class CaptainMarvelShootingStar extends Card {

    public CaptainMarvelShootingStar() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), exileAndGainLife())
                .addTrigger(Triggers.attacks(), exileAndGainLife());

        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                        ObjectRelation.ANY),
                new GainLifeEffect(new EventValue()));
    }

    /** "Exile up to one target creature. That creature's controller gains life equal to its power." */
    private static CardEffect exileAndGainLife() {
        return SequenceEffect.of(new ExileTargetPermanentEffect(),
                new GainLifeEffect(new TargetPower(), Subjects.controllerOf(Subjects.target())));
    }
}
