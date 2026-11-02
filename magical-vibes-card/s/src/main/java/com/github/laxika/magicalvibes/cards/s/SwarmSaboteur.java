package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "13")
public class SwarmSaboteur extends Card {

    public SwarmSaboteur() {
        addNinjutsu("{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ConjureCardToHandEffect("NEO", "128"));
    }
}
