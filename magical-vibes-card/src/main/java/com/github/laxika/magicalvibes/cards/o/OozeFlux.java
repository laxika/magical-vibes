package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledCreaturesCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "128")
public class OozeFlux extends Card {

    public OozeFlux() {
        // The number of counters removed is the activation-time X, so the token's power and
        // toughness both read it back with XValue.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new RemoveOneOrMoreCountersFromControlledCreaturesCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect("Ooze", new XValue(), new XValue(),
                                CardColor.GREEN, List.of(CardSubtype.OOZE), Set.of(), Set.of())),
                "{1}{G}, Remove one or more +1/+1 counters from among creatures you control: "
                        + "Create an X/X green Ooze creature token, where X is the number of +1/+1 counters removed this way.")
                .withXValueFromControlledCreatureCounters());
    }
}
