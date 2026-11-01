package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "259")
@CardRegistration(set = "EMA", collectorNumber = "164")
@CardRegistration(set = "DDU", collectorNumber = "8")
@CardRegistration(set = "SLD", collectorNumber = "762")
public class ElvishVanguard extends Card {

    public ElvishVanguard() {
        // Whenever another Elf enters the battlefield, put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        true));
    }
}
