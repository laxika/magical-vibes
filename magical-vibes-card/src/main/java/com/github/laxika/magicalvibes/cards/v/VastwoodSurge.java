package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "217")
public class VastwoodSurge extends Card {

    public VastwoodSurge() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2,
                        new PermanentIsCreaturePredicate())));
    }
}
