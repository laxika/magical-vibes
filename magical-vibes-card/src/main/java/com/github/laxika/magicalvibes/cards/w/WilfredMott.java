package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "32")
@CardRegistration(set = "WHO", collectorNumber = "350")
public class WilfredMott extends Card {

    public WilfredMott() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.TIME),
                new LookAtTopCardsEffect(
                        new CountersOnSource(CounterType.TIME),
                        new Fixed(1),
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false,
                        LibrarySearchDestination.BATTLEFIELD,
                        true,
                        false,
                        new Fixed(3))));
    }
}
