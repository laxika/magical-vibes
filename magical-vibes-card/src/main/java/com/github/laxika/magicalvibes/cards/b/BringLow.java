package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "KTK", collectorNumber = "103")
public class BringLow extends Card {

    public BringLow() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new FixedIfTargetMatches(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                5,
                3
        )));
    }
}
