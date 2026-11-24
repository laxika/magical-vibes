package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YECL", collectorNumber = "27")
public class ThornaAndTwigtooth extends Card {

    public ThornaAndTwigtooth() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.attacks(),
                SequenceEffect.of(
                        RemoveAllCountersFromTargetPermanentEffect.fromTargetCreature(),
                        new LoseLifeEffect(new EventValue(), Subjects.eachOpponent()),
                        new GainLifeEffect(new EventValue()),
                        new PerpetuallyBoostCardEffect(null, new EventValue(), new EventValue(),
                                Subjects.topmostCardInYourLibrary(new CardTypePredicate(CardType.CREATURE)))));
    }
}
