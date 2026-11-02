package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "90")
@CardRegistration(set = "MSC", collectorNumber = "411")
public class PowerPack extends Card {

    public PowerPack() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect());
    }
}
