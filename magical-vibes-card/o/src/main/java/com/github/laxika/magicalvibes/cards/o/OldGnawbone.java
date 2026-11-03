package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "197")
@CardRegistration(set = "SLD", collectorNumber = "1016")
public class OldGnawbone extends Card {

    public OldGnawbone() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
