package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNoColoredManaSpentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "242")
public class VoidMirror extends Card {

    public VoidMirror() {
        addTrigger(Triggers.playerCastsSpell(),
                new CounterSpellIfNoColoredManaSpentEffect());
    }
}
