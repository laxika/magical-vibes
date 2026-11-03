package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "216")
@CardRegistration(set = "SLC", collectorNumber = "1")
@CardRegistration(set = "SLC", collectorNumber = "28")
public class AltarOfTheBrood extends Card {

    public AltarOfTheBrood() {
        addTrigger(Triggers.enters(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        ObjectRelation.YOU_CONTROL),
                new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
