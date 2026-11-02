package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "239")
public class UndercoverCrocodelf extends Card {

    public UndercoverCrocodelf() {
        addMorph("{3}{G/U}{G/U}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofClueToken(1));
    }
}
