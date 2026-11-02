package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "17")
@CardRegistration(set = "DDO", collectorNumber = "22")
@CardRegistration(set = "E01", collectorNumber = "18")
public class PrecinctCaptain extends Card {

    public PrecinctCaptain() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.whiteSoldier(1));
    }
}
