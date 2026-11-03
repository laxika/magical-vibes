package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "129")
public class KozileksSentinel extends Card {

    public KozileksSentinel() {
        // Whenever you cast a colorless spell, this creature gets +1/+0 until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsColorlessPredicate()), new BoostSelfEffect(1, 0));
    }
}
