package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "120")
public class KnuckleboneWitch extends Card {

    public KnuckleboneWitch() {
        // Whenever a Goblin you control is put into a graveyard from the battlefield,
        // you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.dies(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                                "Put a +1/+1 counter on Knucklebone Witch?")));
    }
}
