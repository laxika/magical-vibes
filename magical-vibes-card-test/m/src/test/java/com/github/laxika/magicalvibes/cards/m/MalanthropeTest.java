package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TormodTheDesecrator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Malanthrope.class, Forest.class, TormodTheDesecrator.class})
class MalanthropeTest extends BaseCardTest {

    @Test
    void exilesTargetPlayersGraveyardAndGetsCountersForCreatureCards() {
        Card firstCreature = new Malanthrope();
        Card secondCreature = new Malanthrope();
        Card noncreature = new Forest();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature, noncreature));

        Permanent malanthrope = castMalanthrope(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature, noncreature);
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void exilesNoncreatureCardsEvenWhenNoCreatureCardsArePresent() {
        Card firstNoncreature = new Forest();
        Card secondNoncreature = new Forest();
        harness.setGraveyard(player2, List.of(firstNoncreature, secondNoncreature));

        Permanent malanthrope = castMalanthrope(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canExileItsControllersGraveyardWithoutAffectingTheOpponent() {
        Card creature = new Malanthrope();
        Card land = new Forest();
        Card opponentsCard = new Malanthrope();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(opponentsCard));

        Permanent malanthrope = castMalanthrope(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canTargetAnEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());

        Permanent malanthrope = castMalanthrope(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsAndExilesTheCardsPresentWhenTheTriggerResolves() {
        Card originalCreature = new Malanthrope();
        Card replacementLand = new Forest();
        harness.setGraveyard(player2, List.of(originalCreature));
        harness.setHand(player1, List.of(new Malanthrope()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player2, List.of(replacementLand));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(replacementLand);
        assertThat(findPermanent(player1, "Malanthrope").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @CardUsed(TormodTheDesecrator.class)
    void exilesMixedGraveyardAsOneEventForTormod() {
        harness.addToBattlefield(player1, new TormodTheDesecrator());
        Card creature = new Malanthrope();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));

        Permanent malanthrope = castMalanthrope(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanent(player1, "Zombie").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castMalanthrope(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Malanthrope()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Malanthrope");
    }
}
