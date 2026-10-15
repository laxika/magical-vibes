package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyGrantStaticEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "28")
public class SparasBodyguard extends Card {

    public SparasBodyguard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardFromHandToPerpetuallyGrantStaticEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)),
                        new PutCountersOnSourceCardEffect(CounterType.SHIELD)));

        PermanentAllOfPredicate otherCreaturesWithShield = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.SHIELD),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        PermanentCounterSum shieldCounters = new PermanentCounterSum(
                CounterType.SHIELD, otherCreaturesWithShield, CountScope.CONTROLLER);
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostSelfEffect(shieldCounters, shieldCounters));
    }
}
