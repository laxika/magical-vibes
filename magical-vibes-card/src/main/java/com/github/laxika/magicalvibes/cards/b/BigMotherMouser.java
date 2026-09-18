package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "37")
@CardRegistration(set = "TMC", collectorNumber = "97")
public class BigMotherMouser extends Card {

    public BigMotherMouser() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addEffect(EffectSlot.ON_ATTACK, new DoublePlusOneCountersOnSourceEffect());
        addEffect(EffectSlot.ON_DEATH, new CreateTokensForEachDyingSourceCounterEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CreateTokenEffect("Robot", 1, 1, null,
                        List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT))));
    }
}
