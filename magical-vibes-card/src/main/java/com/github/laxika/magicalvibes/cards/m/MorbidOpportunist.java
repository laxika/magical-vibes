package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "INR", collectorNumber = "124")
public class MorbidOpportunist extends Card {

    public MorbidOpportunist() {
        // Whenever one or more other creatures die, draw a card. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OncePerTurnTriggerEffect(new DrawCardEffect(1)));
    }
}
