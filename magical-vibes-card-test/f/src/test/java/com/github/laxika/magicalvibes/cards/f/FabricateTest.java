package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fabricate.class, Ornithopter.class, Forest.class})
class FabricateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fabricate puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Fabricate.class);
    }

    @Test
    @DisplayName("Resolving Fabricate presents only artifact cards for choice")
    void resolvingPresentsOnlyArtifacts() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.ARTIFACT));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact card puts it into hand")
    void choosingArtifactPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.hasType(CardType.ARTIFACT));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Choosing an artifact card reveals it and shuffles the library")
    void choosingArtifactRevealsAndShufflesLibrary() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals") && entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Player can fail to find with Fabricate")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Resolving with no artifact cards does not prompt for choice")
    void noArtifactsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no artifact cards"));
    }

    @Test
    @DisplayName("Resolving with empty library does not prompt for choice")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("it is empty"));
    }

    @Test
    @DisplayName("Fabricate goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Fabricate");
    }

    @Test
    @DisplayName("Fabricate takes only the selected artifact from its controller's library")
    void choosesOneOfMultipleArtifactsFromOwnLibrary() {
        Ornithopter firstArtifact = new Ornithopter();
        Ornithopter selectedArtifact = new Ornithopter();
        Forest land = new Forest();
        Ornithopter opposingArtifact = new Ornithopter();
        harness.setLibrary(player1, List.of(land, firstArtifact, selectedArtifact));
        harness.setLibrary(player2, List.of(opposingArtifact));
        setupAndCast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(firstArtifact, selectedArtifact);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selectedArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, firstArtifact);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingArtifact);
        harness.assertNotInHand(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fabricate");
    }

    @Test
    @DisplayName("Failing to find still shuffles and leaves all library cards in the library")
    void failingToFindStillShuffles() {
        Ornithopter artifact = new Ornithopter();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(artifact, land));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(artifact, land);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("chooses not to take a card")
                        && entry.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fabricate");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Ornithopter(), new Forest(), new Forest()));
    }
}
