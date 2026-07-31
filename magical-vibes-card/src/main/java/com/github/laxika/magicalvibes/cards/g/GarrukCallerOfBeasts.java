package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GarrukCallerOfBeastsEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "172")
public class GarrukCallerOfBeasts extends Card {

    public GarrukCallerOfBeasts() {
        // +1: Reveal the top five cards of your library. Put all creature cards revealed this way
        // into your hand and the rest on the bottom of your library in any order.
        // chooseCount == lookCount makes the pick choice-free: every revealed creature auto-moves.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardsEffect(
                        new Fixed(5), new Fixed(5),
                        new CardTypePredicate(CardType.CREATURE),
                        LookDestination.BOTTOM_OF_LIBRARY, true)),
                "+1: Reveal the top five cards of your library. Put all creature cards revealed this way into your hand and the rest on the bottom of your library in any order."
        ));

        // −3: You may put a green creature card from your hand onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardColorPredicate(CardColor.GREEN))),
                                "green creature"),
                        "Put a green creature card from your hand onto the battlefield?")),
                "−3: You may put a green creature card from your hand onto the battlefield."
        ));

        // −7: You get an emblem with "Whenever you cast a creature spell, you may search your
        // library for a creature card, put it onto the battlefield, then shuffle."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new GarrukCallerOfBeastsEmblemEffect()),
                "−7: You get an emblem with \"Whenever you cast a creature spell, you may search your library for a creature card, put it onto the battlefield, then shuffle.\""
        ));
    }
}
