package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnCombatOpponentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "93")
@CardRegistration(set = "DDI", collectorNumber = "48")
public class AetherMembrane extends Card {

    public AetherMembrane() {
        addTrigger(Triggers.blocks(), new ReturnCombatOpponentToHandAtEndOfCombatEffect());
    }
}
