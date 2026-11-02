package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessWaterbendsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "77")
public class TheUnagiOfKyoshiIsland extends Card {

    public TheUnagiOfKyoshiIsland() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessWaterbendsEffect(4));
        addTrigger(Triggers.opponentDrawsCard(),
                new NthCardDrawTriggerEffect(2, new DrawCardEffect(2)));
    }
}
