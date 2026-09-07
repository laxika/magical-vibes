package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "VOW", collectorNumber = "46")
public class WelcomingVampire extends Card {

    public WelcomingVampire() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMaxPowerConditionalEffect(2,
                        new OncePerTurnTriggerEffect(new DrawCardEffect(1))));
    }
}
