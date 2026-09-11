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

@CardRegistration(set = "HOB", collectorNumber = "147")
public class BifurMelodicRider extends Card {

    public BifurMelodicRider() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.STATIC, new AdditionalTriggeredAbilityEffect(
                new PermanentHasSubtypePredicate(CardSubtype.DWARF),
                new ControllerHasEnduringStory(), true, false));

        target(TargetFilters.creature());
        PutCounterOnTargetPermanentEffect counter =
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, counter);
        addEffect(EffectSlot.ON_ATTACK, counter);
    }
}
