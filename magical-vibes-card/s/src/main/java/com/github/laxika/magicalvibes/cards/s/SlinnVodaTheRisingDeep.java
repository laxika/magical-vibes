package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "66")
public class SlinnVodaTheRisingDeep extends Card {

    public SlinnVodaTheRisingDeep() {
        // Kicker {1}{U}
        addStaticEffect(new KickerEffect("{1}{U}"));

        // When Slinn Voda, the Rising Deep enters, if it was kicked, return all
        // creatures to their owners' hands except for Merfolk, Krakens, Leviathans,
        // Octopuses, and Serpents.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(Set.of(
                                        CardSubtype.MERFOLK, CardSubtype.KRAKEN, CardSubtype.LEVIATHAN,
                                        CardSubtype.OCTOPUS, CardSubtype.SERPENT
                                )))
                        ))));
    }
}
