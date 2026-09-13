package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinChariot.class, GoblinMatron.class, GoblinPatrol.class, GoblinRaider.class, Island.class})
class GoblinMatronTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability only offers Goblin cards")
    void acceptingMayOffersGoblins() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactly(gd.playerDecks.get(player1.getId()).get(0),
                        gd.playerDecks.get(player1.getId()).get(1));
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Choosing a Goblin puts it into the owner's hand")
    void choosingPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosenCard);
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
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

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island);
        assertThat(gameLogContains("finds no Goblin cards")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Choosing a later matching Goblin puts that selected card into hand")
    void choosingLaterMatchingGoblinPutsSelectedCardIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(c -> c.getName())
                .containsExactly("Goblin Raider", "Goblin Chariot");

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Goblin Chariot")
                .doesNotContain("Goblin Raider");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Goblin Raider", "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GoblinMatron()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GoblinRaider(), new GoblinChariot(), new Island()));
    }
}
