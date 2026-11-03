package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "236")
public class LevitatingStatue extends Card {

    public LevitatingStatue() {
        // Whenever you cast a noncreature spell, put a +1/+1 counter on this artifact.
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // {2}: This artifact becomes a 1/1 Construct artifact creature until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AnimatePermanentsEffect(1, 1, List.of(CardSubtype.CONSTRUCT), Set.of())),
                "{2}: This artifact becomes a 1/1 Construct artifact creature until end of turn."
        ));
    }
}
