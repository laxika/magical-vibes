package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GurmagNightwatch.class, Forest.class, Island.class})
class GurmagNightwatchTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers the top three cards and keeps the chosen card on top")
    void etbKeepsChosenCardOnTopAndGravesTheRest() {
        Card chosen = new Forest();
        Card restOne = new Island();
        Card restTwo = new Island();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
    }

    @Test
    @DisplayName("Declining the ETB choice puts all three cards into the graveyard")
    void decliningGravesAllLookedAtCards() {
        Card first = new Forest();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    @Test
    void choosingThirdCardPreservesUnlookedAtLibraryAndOpponentsZones() {
        Card first = new Forest();
        Card second = new Island();
        Card chosen = new GurmagNightwatch();
        Card fourth = new Island();
        Card fifth = new Forest();
        Card opponentCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, chosen, fourth, fifth));
        harness.setLibrary(player2, List.of(opponentCard));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen, fourth, fifth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void shortLibraryAllowsKeepingOneOfTwoCards() {
        Card first = new Forest();
        Card chosen = new Island();
        harness.setLibrary(player1, List.of(first, chosen));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void singleCardLibraryStillAllowsDeclining() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void emptyLibraryResolvesWithoutRequiringAChoice() {
        harness.setLibrary(player1, List.of());

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Gurmag Nightwatch");
    }

    private void castGurmagNightwatch() {
        harness.setHand(player1, List.of(new GurmagNightwatch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }
}
