package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "178")
@CardRegistration(set = "PIO", collectorNumber = "230")
public class LoxodonSmiter extends Card {

    public LoxodonSmiter() {
        addStaticEffect(new CantBeCounteredEffect());
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new EnterBattlefieldOnDiscardEffect());
    }
}
