package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "149")
@CardRegistration(set = "FDN", collectorNumber = "303")
@CardRegistration(set = "KHM", collectorNumber = "382")
@CardRegistration(set = "SLD", collectorNumber = "1588")
@CardRegistration(set = "FDC", collectorNumber = "58")
public class YouthfulValkyrie extends Card {

    public YouthfulValkyrie() {
        // Whenever another Angel you control enters, put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ANGEL),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
