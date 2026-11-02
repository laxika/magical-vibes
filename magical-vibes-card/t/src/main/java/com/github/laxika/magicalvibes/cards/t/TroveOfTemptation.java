package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "171")
public class TroveOfTemptation extends Card {

    public TroveOfTemptation() {
        // Each opponent must attack you or a planeswalker you control with at least one creature each combat if able.
        addStaticEffect(new OpponentsMustAttackControllerEffect());

        // At the beginning of your end step, create a Treasure token.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), CreateTokenEffect.ofTreasureToken(1));
    }
}
