package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealmSeekers.class, Forest.class, GrizzlyBears.class})
class RealmSeekersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters equal to the cards in all players' hands")
    void entersWithCountersFromAllPlayersHands() {
        harness.setHand(player1, List.of(new RealmSeekers(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent seekers = findPermanent(player1, "Realm Seekers");
        assertThat(seekers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a counter and searches the library for a land")
    void removesCounterAndSearchesForLand() {
        Permanent seekers = addCreatureReady(player1, new RealmSeekers());
        seekers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");

        harness.handleCardChosen(player1, 0);

        assertThat(seekers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        addCreatureReady(player1, new RealmSeekers());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    void countsHandsAtResolutionRatherThanAtCasting() {
        harness.setHand(player1, List.of(new RealmSeekers(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);

        harness.setHand(player2, List.of(new Forest(), new Forest(), new GrizzlyBears()));
        harness.passBothPriorities();

        Permanent seekers = findPermanent(player1, "Realm Seekers");
        assertThat(seekers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.setHand(player2, List.of());
        assertThat(seekers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void diesWhenEnteringWithBothHandsEmpty() {
        harness.setHand(player1, List.of(new RealmSeekers()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Realm Seekers");
        harness.assertInGraveyard(player1, "Realm Seekers");
    }

    @Test
    void abilityStillSearchesAfterRemovingLastCounterKillsSource() {
        Permanent seekers = addCreatureReady(player1, new RealmSeekers());
        seekers.setSummoningSick(true);
        seekers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(seekers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Realm Seekers");
        harness.assertInGraveyard(player1, "Realm Seekers");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void mayFailToFindEvenWithLandInLibrary() {
        Permanent seekers = addCreatureReady(player1, new RealmSeekers());
        seekers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(seekers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
