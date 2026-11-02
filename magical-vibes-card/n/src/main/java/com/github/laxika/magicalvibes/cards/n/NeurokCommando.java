package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "28")
public class NeurokCommando extends Card {

    public NeurokCommando() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
