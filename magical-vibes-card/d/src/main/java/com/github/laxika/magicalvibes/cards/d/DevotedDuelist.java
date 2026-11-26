package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "104")
public class DevotedDuelist extends Card {

    public DevotedDuelist() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
