package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "133")
@CardRegistration(set = "KLR", collectorNumber = "148")
public class SpiresideInfiltrator extends Card {

    public SpiresideInfiltrator() {
        // Whenever this creature becomes tapped, it deals 1 damage to each opponent.
        addTrigger(Triggers.becomesTapped(), DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
