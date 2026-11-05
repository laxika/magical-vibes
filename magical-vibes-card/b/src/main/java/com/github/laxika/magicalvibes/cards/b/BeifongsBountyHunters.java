package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "209")
public class BeifongsBountyHunters extends Card {

    private static final PermanentNotPredicate NONLAND =
            new PermanentNotPredicate(new PermanentIsLandPredicate());
    private static final CardEffect ALLY_DEATH_TRIGGER =
            new EarthbendTargetLandEffect(new EventValue());

    public BeifongsBountyHunters() {
        addTrigger(Triggers.creatureDies(NONLAND, ObjectRelation.YOU_CONTROL), ALLY_DEATH_TRIGGER);
    }
}
