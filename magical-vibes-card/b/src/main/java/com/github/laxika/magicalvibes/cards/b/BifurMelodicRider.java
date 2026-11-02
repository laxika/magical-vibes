package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "147")
@CardRegistration(set = "HOC", collectorNumber = "147")
public class BifurMelodicRider extends Card {

    public BifurMelodicRider() {
        addStaticEffect(new StoriedEffect());
        addStaticEffect(new AdditionalTriggeredAbilityEffect(
                new PermanentHasSubtypePredicate(CardSubtype.DWARF),
                new ControllerHasEnduringStory(), true, false));

        target(TargetFilters.creature());
        PutCounterOnTargetPermanentEffect counter =
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addTrigger(Triggers.enters(ObjectRelation.THIS), counter);
        addEffect(EffectSlot.ON_ATTACK, counter);
    }
}
