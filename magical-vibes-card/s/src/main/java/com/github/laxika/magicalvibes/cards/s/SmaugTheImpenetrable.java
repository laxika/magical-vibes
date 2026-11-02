package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "9")
public class SmaugTheImpenetrable extends Card {

    public SmaugTheImpenetrable() {
        addTrigger(Triggers.dealtNoncombatDamage(),
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
