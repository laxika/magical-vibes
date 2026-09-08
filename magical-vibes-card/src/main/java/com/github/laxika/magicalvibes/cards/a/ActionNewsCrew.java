package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "1")
public class ActionNewsCrew extends Card {

    public ActionNewsCrew() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect()
                ),
                "Channel — {6}, Discard this card: Put a +1/+1 counter on each creature you control. Draw a card."
        ));
    }
}
