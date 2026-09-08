package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "190")
public class InstrumentOfTheBards extends Card {

    public InstrumentOfTheBards() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.HARMONY),
                        "Put a harmony counter on Instrument of the Bards?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(
                        new SearchLibraryAndConditionalEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.HAND,
                                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                                CreateTokenEffect.ofTreasureToken(1),
                                new ManaValueBound(new CountersOnSource(CounterType.HARMONY), true, 0),
                                false),
                        new ShuffleLibraryEffect(false)),
                "{3}{G}, {T}: Search your library for a creature card with mana value equal to the number of harmony counters on Instrument of the Bards, reveal it, and put it into your hand. If that card is legendary, create a Treasure token. Then shuffle."
        ));
    }
}
