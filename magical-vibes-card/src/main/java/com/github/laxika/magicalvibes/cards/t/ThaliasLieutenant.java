package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "43")
public class ThaliasLieutenant extends Card {

    public ThaliasLieutenant() {
        // When this creature enters, put a +1/+1 counter on each other Human you control.
        var otherHumans = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, otherHumans));

        // Whenever another Human you control enters, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.HUMAN),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
