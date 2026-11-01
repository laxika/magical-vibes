package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "79")
@CardRegistration(set = "INR", collectorNumber = "365")
@CardRegistration(set = "VOW", collectorNumber = "70")
@CardRegistration(set = "DBL", collectorNumber = "337")
public class Necroduality extends Card {

    public Necroduality() {
        // Whenever a nontoken Zombie you control enters, create a token that's a copy of that creature.
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new CreateTokenCopyOfTargetPermanentEffect()));
    }
}
