package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "169")
public class TilonallisKnight extends Card {

    public TilonallisKnight() {
        // Whenever this creature attacks, if you control a Dinosaur,
        // this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.attacks(),
                new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)), new BoostSelfEffect(1, 1)));
    }
}
