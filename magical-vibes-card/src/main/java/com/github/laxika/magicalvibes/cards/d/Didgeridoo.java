package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "105")
public class Didgeridoo extends Card {

    public Didgeridoo() {
        // {3}: You may put a Minotaur permanent card from your hand onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardSubtypePredicate(CardSubtype.MINOTAUR))),
                                "Minotaur permanent"),
                        "Put a Minotaur permanent card from your hand onto the battlefield?"
                )),
                "{3}: You may put a Minotaur permanent card from your hand onto the battlefield."
        ));
    }
}
