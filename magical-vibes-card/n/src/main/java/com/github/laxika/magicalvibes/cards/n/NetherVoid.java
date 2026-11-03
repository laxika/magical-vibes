package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "113")
@CardRegistration(set = "ME3", collectorNumber = "73")
public class NetherVoid extends Card {

    public NetherVoid() {
        addTrigger(Triggers.playerCastsSpell().anySpell(), new CounterUnlessPaysEffect(3));
    }
}
