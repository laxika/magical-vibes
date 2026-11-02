package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "706")
public class StickFearlessMentor extends Card {

    public StickFearlessMentor() {
        addTrigger(Triggers.youAreDealtDamageBySourceYouControl(),
                new OncePerTurnTriggerEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(1)));
    }
}
