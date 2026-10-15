package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

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
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, ALLY_DEATH_TRIGGER);
        addEffect(EffectSlot.ON_DEATH, SELF_DEATH_TRIGGER);
    }
}
