package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "93")
public class PhantomWhelp extends Card {

    public PhantomWhelp() {
        addTrigger(Triggers.attacks(), new ReturnSelfToHandAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new ReturnSelfToHandAtEndOfCombatEffect());
    }
}
