package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.cards.s.StripedBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BuriedAlive.class, StripedBears.class, NullRod.class})
class BuriedAliveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only creature cards for a graveyard search")
    void offersCreaturesOnly() {
        castBuriedAlive();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsOnly("Striped Bears");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Three chosen creatures all go to the graveyard")
    void threeCreaturesToGraveyard() {
        castBuriedAlive();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        for (int i = 0; i < 3; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Striped Bears")).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Striped Bears"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search stops after finding the only two matching creatures")
    void onlyTwoCreatures() {
        castBuriedAlive();
        harness.setLibrary(player1, List.of(new NullRod(), new StripedBears(), new StripedBears(), new NullRod()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsOnly("Striped Bears");
        assertThat(search.params().cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Striped Bears")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsOnly("Null Rod");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find early ends the search with fewer creatures")
    void failToFindEarly() {
        castBuriedAlive();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Striped Bears")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Buried Alive");
    }

    @Test
    @DisplayName("No creatures in the library finishes without a prompt")
    void noCreatures() {
        castBuriedAlive();
        harness.setLibrary(player1, List.of(new NullRod(), new NullRod()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsOnly("Null Rod");
        harness.assertInGraveyard(player1, "Buried Alive");
    }

    private void castBuriedAlive() {
        harness.castFromHand(player1, new BuriedAlive(), "{2}{B}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new NullRod(), new StripedBears(), new StripedBears(), new StripedBears(), new NullRod()));
    }
}
