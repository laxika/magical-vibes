package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TMC", collectorNumber = "15")
@CardRegistration(set = "TMC", collectorNumber = "87")
public class BebopSkullCrossbones extends Card {

    private static final String PARTNER_NAME = "Rocksteady, Mutant Marauder";

    public BebopSkullCrossbones() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetLibraryEffect(
                        1,
                        new CardNamedPredicate(PARTNER_NAME),
                        LibrarySearchDestination.HAND,
                        true,
                        LibrarySearchPlayer.TARGET_PLAYER),
                "Search your library for " + PARTNER_NAME + "?",
                null,
                MayChoicePlayer.TARGET_PLAYER
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                new DrawAndLoseLifePerCounterOnSourceEffect(),
                "Draw cards equal to the number of counters on Bebop?"));
    }
}
