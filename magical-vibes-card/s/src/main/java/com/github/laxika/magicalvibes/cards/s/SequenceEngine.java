package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TargetCardsManaValueSum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "67")
public class SequenceEngine extends Card {

    public SequenceEngine() {
        CardAllOfPredicate creatureWithManaValueX = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardManaValueEqualsXPredicate()));
        CreateTokenEffect fractal = new CreateTokenEffect(
                "Fractal", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new ExileTargetCardFromGraveyardThenEffect(
                        creatureWithManaValueX,
                        new CreateXTokenWithXCountersEffect(
                                fractal, new TargetCardsManaValueSum(), CounterType.PLUS_ONE_PLUS_ONE))),
                "{X}, {T}: Exile target creature card with mana value X from a graveyard. Create a 0/0 "
                        + "green and blue Fractal creature token. Put X +1/+1 counters on it. Activate only "
                        + "as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
