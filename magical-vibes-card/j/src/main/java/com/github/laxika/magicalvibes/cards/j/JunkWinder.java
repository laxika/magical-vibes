package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "48")
@CardRegistration(set = "MOC", collectorNumber = "225")
@CardRegistration(set = "MKC", collectorNumber = "107")
@CardRegistration(set = "BLC", collectorNumber = "169")
@CardRegistration(set = "HBG", collectorNumber = "903")
public class JunkWinder extends Card {

    public JunkWinder() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsTokenPredicate(), CountScope.CONTROLLER)));

        target(TargetFilters.nonlandPermanentAnOpponentControls()).addTrigger(
                new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.DEFAULT,
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
