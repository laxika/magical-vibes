package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "229")
public class HangarbackWalker extends Card {

    public HangarbackWalker() {
        // This creature enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        // When this creature dies, create a 1/1 colorless Thopter artifact creature token
        // with flying for each +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_DEATH, new CreateTokensForEachDyingSourceCounterEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CreateTokenEffect("Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT))));

        // {1}, {T}: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}, {T}: Put a +1/+1 counter on this creature."));
    }
}
