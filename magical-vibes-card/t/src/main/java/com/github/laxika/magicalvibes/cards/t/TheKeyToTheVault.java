package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayCastExiledNonlandCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "54")
public class TheKeyToTheVault extends Card {

    public TheKeyToTheVault() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LookAtTopCardsMayCastExiledNonlandCardEffect(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}{U}"));
    }
}
