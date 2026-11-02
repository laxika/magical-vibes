package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "113")
@CardRegistration(set = "ME3", collectorNumber = "73")
public class NetherVoid extends Card {

    public NetherVoid() {
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(null, List.of(new CounterUnlessPaysEffect(3))));
    }
}
