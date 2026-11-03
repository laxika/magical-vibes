package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "157")
public class TeapotSlinger extends Card {

    public TeapotSlinger() {
        addTrigger(Triggers.youCastSpell().expend(4), new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
    }
}
