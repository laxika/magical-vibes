package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "MSC", collectorNumber = "706")
public class StickFearlessMentor extends Card {

    public StickFearlessMentor() {
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE,
                new OncePerTurnTriggerEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(1)));
    }
}
