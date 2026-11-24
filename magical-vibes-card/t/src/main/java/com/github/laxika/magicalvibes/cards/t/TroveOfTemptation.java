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
        addStaticEffect(new OpponentsMustAttackControllerEffect());

        addTrigger(Triggers.endStep(ActivePlayer.YOU), CreateTokenEffect.ofTreasureToken(1));
    }
}
