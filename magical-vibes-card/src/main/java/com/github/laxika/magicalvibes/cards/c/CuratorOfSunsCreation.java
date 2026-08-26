package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "LCI", collectorNumber = "141")
public class CuratorOfSunsCreation extends Card {

    public CuratorOfSunsCreation() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCOVERS,
                new OncePerTurnTriggerEffect(new DiscoverEffect(new EventValue())));
    }
}
