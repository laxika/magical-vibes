package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "589")
public class CaptainMarvelShootingStar extends Card {

    public CaptainMarvelShootingStar() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect())
                .addTrigger(Triggers.attacks(),
                        new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect());

        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                        ObjectRelation.ANY),
                new GainLifeEffect(new EventValue()));
    }
}
