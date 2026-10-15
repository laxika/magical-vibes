package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "4")
public class TheThing extends Card {

    public TheThing() {
        // At the beginning of combat on your turn, if you've cast a noncreature spell this turn,
        // put four +1/+1 counters on The Thing.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)));

        // Whenever The Thing attacks, you may pay {R}{G}{W}{U}. When you do, double the number
        // of each kind of counter on any number of target permanents you control.
        target(TargetFilters.permanentYouControl(), 0, 99)
                .addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                        "{R}{G}{W}{U}",
                        new DoubleCountersOnTargetPermanentEffect(),
                        "Pay {R}{G}{W}{U} to double the counters on your permanents?"));
    }
}
