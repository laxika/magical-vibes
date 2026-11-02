package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "90")
public class FirefistStriker extends Card {

    public FirefistStriker() {
        // Battalion — Whenever this creature and at least two other creatures attack,
        // target creature can't block this turn.
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new MinimumAttackers(3),
                new CantBlockThisTurnEffect(TapUntapScope.TARGET)));
    }
}
