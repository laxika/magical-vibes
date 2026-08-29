package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JTunGruntTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep puts two cards from one graveyard on library bottoms")
    void paysCumulativeUpkeep() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card libraryCard = new GrizzlyBears();
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
        Card ownFirst = new GrizzlyBears();
        Card ownSecond = new Forest();
        Card ownUnused = new GrizzlyBears();
        Card ownUnusedSecond = new Forest();
        Card opponentFirst = new GrizzlyBears();
        Card opponentSecond = new Forest();
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
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, grunt.getCard());
    }

    @Test
    @DisplayName("Cards from different graveyards cannot be combined for one payment")
    void cannotCombineGraveyardsForOnePayment() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new JTunGrunt());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard, grunt.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }
}
