package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "KTK", collectorNumber = "160")
public class AbzanAscendancy extends Card {

    public AbzanAscendancy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                1,
                new PermanentIsCreaturePredicate()
        ));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CreateTokenEffect.whiteSpirit(1));
    }
}
