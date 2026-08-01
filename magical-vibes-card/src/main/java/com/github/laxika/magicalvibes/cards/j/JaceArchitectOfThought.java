package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedOpponentAttackerBoostEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;
import com.github.laxika.magicalvibes.model.effect.SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "44")
public class JaceArchitectOfThought extends Card {

    public JaceArchitectOfThought() {
        // +1: Until your next turn, whenever a creature an opponent controls attacks, it gets -1/-0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RegisterDelayedOpponentAttackerBoostEffect(-1, 0)),
                "+1: Until your next turn, whenever a creature an opponent controls attacks, it gets -1/-0 until end of turn."
        ));

        // −2: Reveal the top three cards of your library. An opponent separates those cards into two
        // piles. Put one pile into your hand and the other on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new RevealTopCardsAndSeparateEffect(3, CardPileDisposition.HAND_AND_BOTTOM)),
                "−2: Reveal the top three cards of your library. An opponent separates those cards into two piles. "
                        + "Put one pile into your hand and the other on the bottom of your library in any order."
        ));

        // −8: For each player, search that player's library for a nonland card and exile it, then that
        // player shuffles. You may cast those cards without paying their mana costs.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect()),
                "−8: For each player, search that player's library for a nonland card and exile it, then that player "
                        + "shuffles. You may cast those cards without paying their mana costs."
        ));
    }
}
