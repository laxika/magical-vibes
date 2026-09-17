package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOC", collectorNumber = "99")
public class GandalfShadowsFoe extends Card {

    public GandalfShadowsFoe() {
        // When Gandalf enters, exile up to three target lands you control, then return them to the
        // battlefield tapped under their owners' control.
        target(TargetFilters.landYouControl(), 0, 3)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, FlickerEffect.flickerTargetReturningTapped());

        // Landfall — Whenever a land you control enters, draw a card and put a +1/+1 counter on Gandalf.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
