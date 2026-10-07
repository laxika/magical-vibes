package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TowerWinder.class, CommandTower.class})
class TowerWinderTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches the library for Command Tower")
    void searchesLibraryForCommandTower() {
        CommandTower commandTower = new CommandTower();
        Card otherCard = new TowerWinder();
        setLibrary(commandTower, otherCard);
        castTowerWinder();

        resolveAllTriggers();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(commandTower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(commandTower.getId()));

        harness.assertInHand(player1, "Command Tower");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability searches the graveyard for Command Tower")
    void searchesGraveyardForCommandTower() {
        CommandTower commandTower = new CommandTower();
        harness.setGraveyard(player1, List.of(commandTower));
        castTowerWinder();

        resolveAllTriggers();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(commandTower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(commandTower.getId()));

        harness.assertInHand(player1, "Command Tower");
        harness.assertNotInGraveyard(player1, "Command Tower");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability ignores cards with other names")
    void ignoresOtherCards() {
        TowerWinder otherCard = new TowerWinder();
        setLibrary(otherCard);
        castTowerWinder();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("Only one Command Tower is taken when both zones contain one")
    void choosesOneCardAcrossBothZones() {
        CommandTower libraryTower = new CommandTower();
        CommandTower graveyardTower = new CommandTower();
        setLibrary(libraryTower);
        harness.setGraveyard(player1, List.of(graveyardTower));
        castTowerWinder();
        resolveAllTriggers();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(libraryTower.getId(), graveyardTower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardTower.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardTower);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryTower);
        harness.assertNotInGraveyard(player1, "Command Tower");
    }

    @Test
    @DisplayName("A library search can fail to find a matching card")
    void canFailToFindInLibrary() {
        CommandTower commandTower = new CommandTower();
        setLibrary(commandTower);
        castTowerWinder();
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(commandTower);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search never takes cards from an opponent's zones")
    void doesNotSearchOpponentZones() {
        CommandTower libraryTower = new CommandTower();
        CommandTower graveyardTower = new CommandTower();
        setLibrary();
        harness.setLibrary(player2, List.of(libraryTower));
        harness.setGraveyard(player2, List.of(graveyardTower));
        castTowerWinder();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryTower);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardTower);
    }

    private void castTowerWinder() {
        harness.setHand(player1, List.of(new TowerWinder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
