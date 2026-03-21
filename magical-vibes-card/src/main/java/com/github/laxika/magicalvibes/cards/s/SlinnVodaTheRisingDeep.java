package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "66")
public class SlinnVodaTheRisingDeep extends Card {

    public SlinnVodaTheRisingDeep() {
        // Kicker {1}{U}
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{U}"));

        // When Slinn Voda, the Rising Deep enters, if it was kicked, return all
        // creatures to their owners' hands except for Merfolk, Krakens, Leviathans,
        // Octopuses, and Serpents.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                new ReturnCreaturesToOwnersHandEffect(Set.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(Set.of(
                                        CardSubtype.MERFOLK, CardSubtype.KRAKEN, CardSubtype.LEVIATHAN,
                                        CardSubtype.OCTOPUS, CardSubtype.SERPENT
                                ))),
                                "Must not be a Merfolk, Kraken, Leviathan, Octopus, or Serpent"
                        )
                ))
        ));
    }
}
