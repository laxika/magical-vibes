package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "171")
public class IncubationSac extends Card {

    public IncubationSac() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.OIL),
                        new CreateTokenEffect("Phyrexian Golem", 3, 3, null,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(),
                                Set.of(CardType.ARTIFACT))
                ),
                "{4}, {T}, Remove an oil counter from Incubation Sac: Create a 3/3 colorless "
                        + "Phyrexian Golem artifact creature token. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
