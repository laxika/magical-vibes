package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "120")
@CardRegistration(set = "2X2", collectorNumber = "219")
public class GloryscaleViashino extends Card {

    public GloryscaleViashino() {
        // Whenever you cast a multicolored spell, this creature gets +3/+3 until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()), new BoostSelfEffect(3, 3));
    }
}
