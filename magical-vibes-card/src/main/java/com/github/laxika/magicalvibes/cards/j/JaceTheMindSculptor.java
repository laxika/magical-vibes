package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPlayerHandIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "31")
public class JaceTheMindSculptor extends Card {

    public JaceTheMindSculptor() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.MAY_PUT_TOP_ON_BOTTOM)),
                "+2: Look at the top card of target player's library. You may put that card on the bottom of that player's library.",
                anyPlayer()
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                        3, 2, HandToLibraryPlacement.TOP)),
                "0: Draw three cards, then put two cards from your hand on top of your library in any order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(ReturnToHandEffect.target()),
                "−1: Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -12,
                List.of(new ExileTargetPlayerLibraryEffect(),
                        new ShuffleTargetPlayerHandIntoLibraryEffect()),
                "−12: Exile all cards from target player's library, then that player shuffles their hand into their library.",
                anyPlayer()
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
