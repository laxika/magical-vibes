package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "152")
public class TatteredRatter extends Card {

    public TatteredRatter() {
        // Whenever a Rat you control becomes blocked, it gets +2/+0 until end of turn.
        addTrigger(Triggers.creatureYouControlBecomesBlocked(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.RAT),
                        new BoostSelfEffect(2, 0)));
    }
}
