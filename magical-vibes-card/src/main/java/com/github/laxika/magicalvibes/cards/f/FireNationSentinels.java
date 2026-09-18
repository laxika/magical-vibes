package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TLE", collectorNumber = "230")
public class FireNationSentinels extends Card {

    public FireNationSentinels() {
        // Whenever a nontoken creature an opponent controls dies, put a +1/+1 counter on each
        // creature you control.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardNotPredicate(new CardIsTokenPredicate()),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())));
    }
}
