package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "266")
public class Triskelavus extends Card {

    public Triskelavus() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Triskelavite",
                                1,
                                1,
                                null,
                                null,
                                List.of(CardSubtype.TRISKELAVITE),
                                Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT),
                                false,
                                false,
                                Map.of(),
                                List.of(new ActivatedAbility(
                                        false,
                                        null,
                                        List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                                        "Sacrifice this token: This token deals 1 damage to any target."
                                )),
                                false,
                                false,
                                false,
                                0,
                                Set.of())
                ),
                "{1}, Remove a +1/+1 counter from this creature: Create a 1/1 colorless Triskelavite artifact creature token with flying."
        ));
    }
}
