package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "224")
public class SkyserpentSeeker extends Card {

    public SkyserpentSeeker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect
                                .allMatchingOntoBattlefieldTapped(new Fixed(2), new CardTypePredicate(CardType.LAND)),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Exhaust — {4}: Reveal cards from the top of your library until you reveal two land cards. "
                        + "Put those land cards onto the battlefield tapped and the rest on the bottom of your library "
                        + "in a random order. Put a +1/+1 counter on this creature. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
