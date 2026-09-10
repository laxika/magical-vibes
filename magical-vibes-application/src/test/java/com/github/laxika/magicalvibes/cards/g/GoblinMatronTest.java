package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMatron.class, GoblinRaider.class, Island.class})
class GoblinMatronTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability only offers Goblin cards")
    void acceptingMayOffersGoblins() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.GOBLIN));
    }

    @Test
    @DisplayName("Choosing a Goblin reveals it, puts it into hand, and shuffles the library")
    void choosingPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Accepting with no Goblin cards leaves the library unchanged")
    void noGoblinCardsLeaveLibraryUnchanged() {
        setupAndCast();
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island);
        assertThat(gameLogContains("finds no Goblin cards")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new GoblinMatron(), "{2}{R}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GoblinRaider(), new Island()));
    }
}
