package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "209")
public class BeifongsBountyHunters extends Card {

    private static final PermanentNotPredicate NONLAND =
            new PermanentNotPredicate(new PermanentIsLandPredicate());
    private static final TriggeringPermanentConditionalEffect ALLY_DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    NONLAND, new EarthbendTargetLandEffect(new EventValue()));
    private static final TriggeringPermanentConditionalEffect SELF_DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    NONLAND, new EarthbendTargetLandEffect(new SourcePower()));

    public BeifongsBountyHunters() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), ALLY_DEATH_TRIGGER);
    }
}
