package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FCA", collectorNumber = "12")
@CardRegistration(set = "HBG", collectorNumber = "174")
public class AncientCopperDragon extends Card {

    public AncientCopperDragon() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RollD20Effect(
                CreateTokenEffect.ofTreasureToken(new EventValue()),
                CreateTokenEffect.ofTreasureToken(new EventValue())
        ));
    }
}
