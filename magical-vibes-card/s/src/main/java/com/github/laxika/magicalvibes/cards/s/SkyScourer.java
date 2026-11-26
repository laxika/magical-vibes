package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "78")
public class SkyScourer extends Card {

    public SkyScourer() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsColorlessPredicate()), BoostEffect.self(1, 0));
    }
}
