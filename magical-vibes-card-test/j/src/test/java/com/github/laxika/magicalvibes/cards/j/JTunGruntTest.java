package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JTunGrunt.class, SnowCoveredForest.class})
class JTunGruntTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep puts two cards from one graveyard on library bottoms")
    void paysCumulativeUpkeep() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        Card first = new SnowCoveredForest();
        Card second = new SnowCoveredForest();
        Card libraryCard = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, new ArrayList<>(List.of(libraryCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(grunt.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, first, second);
    }

    @Test
    @DisplayName("Each cumulative upkeep payment may use a different graveyard")
    void choosesOneGraveyardPerPayment() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        grunt.setCounterCount(CounterType.AGE, 1);
        Card ownFirst = new SnowCoveredForest();
        Card ownSecond = new SnowCoveredForest();
        Card ownUnused = new SnowCoveredForest();
        Card ownUnusedSecond = new SnowCoveredForest();
        Card opponentFirst = new SnowCoveredForest();
        Card opponentSecond = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(ownFirst, ownSecond, ownUnused, ownUnusedSecond));
        harness.setGraveyard(player2, List.of(opponentFirst, opponentSecond));
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(ownFirst.getId(), ownSecond.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(opponentFirst.getId(), opponentSecond.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownUnused, ownUnusedSecond);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownFirst, ownSecond);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentFirst, opponentSecond);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the creature")
    void declineSacrifices() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        Card first = new SnowCoveredForest();
        Card second = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, grunt.getCard());
    }

    @Test
    @DisplayName("Cumulative upkeep cannot be paid when no single graveyard has two cards")
    void cannotPayWhenNoSingleGraveyardHasEnoughCards() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        Card ownCard = new SnowCoveredForest();
        Card opponentCard = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard, grunt.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("A cumulative upkeep payment cannot combine cards from different graveyards")
    void rejectsCombiningGraveyardsForOnePayment() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        Card ownFirst = new SnowCoveredForest();
        Card ownSecond = new SnowCoveredForest();
        Card opponentFirst = new SnowCoveredForest();
        Card opponentSecond = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(ownFirst, ownSecond));
        harness.setGraveyard(player2, List.of(opponentFirst, opponentSecond));
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownFirst.getId(), opponentFirst.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownFirst, ownSecond);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentFirst, opponentSecond);

        harness.handleMultipleCardsChosen(player1, List.of(ownFirst.getId(), ownSecond.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentFirst, opponentSecond);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownFirst, ownSecond);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple age counters can be paid from the same graveyard without reusing cards")
    void paysMultipleTimesFromSameGraveyard() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        grunt.setCounterCount(CounterType.AGE, 1);
        Card first = new SnowCoveredForest();
        Card second = new SnowCoveredForest();
        Card third = new SnowCoveredForest();
        Card fourth = new SnowCoveredForest();
        Card libraryCard = new SnowCoveredForest();
        harness.setGraveyard(player2, List.of(first, second, third, fourth));
        harness.setLibrary(player2, List.of(libraryCard));

        advanceToUpkeep(player1);

        assertThat(grunt.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(grunt.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(fourth.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grunt);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, second, first, fourth, third);
    }

    @Test
    @DisplayName("Four cards split three and one between graveyards cannot pay two age counters")
    void cannotPayHigherUpkeepWithUnpairedCards() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        grunt.setCounterCount(CounterType.AGE, 1);
        Card first = new SnowCoveredForest();
        Card second = new SnowCoveredForest();
        Card third = new SnowCoveredForest();
        Card opponentCard = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third, grunt.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        grunt.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(grunt.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grunt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
