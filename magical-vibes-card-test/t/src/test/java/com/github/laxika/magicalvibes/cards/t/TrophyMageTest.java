package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.ImplementOfExamination;
import com.github.laxika.magicalvibes.cards.r.RenegadeMap;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrophyMage.class, ImplementOfExamination.class, RenegadeMap.class, TreasureKeeper.class})
class TrophyMageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Trophy Mage creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        resolveEnterTheBattlefieldTrigger();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability offers only artifacts with mana value 3")
    void acceptingMayPresentsOnlyManaValueThreeArtifacts() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName()).containsExactly("Implement of Examination");
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand")
    void choosingArtifactPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Implement of Examination");
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Failing to find is allowed")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes and shuffles")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library with no matching artifacts completes the search without taking a card")
    void noMatchingArtifactsCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new RenegadeMap(), new TreasureKeeper(), new TrophyMage()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The search uses only its controller's library")
    void doesNotSearchOpponentsLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new TrophyMage()));
        ImplementOfExamination opponentArtifact = new ImplementOfExamination();
        harness.setLibrary(player2, List.of(opponentArtifact));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    @DisplayName("A found artifact is revealed, removed from the library, and the library is shuffled")
    void foundArtifactIsRevealedAndLibraryIsShuffled() {
        setupAndCast();
        ImplementOfExamination artifact = new ImplementOfExamination();
        RenegadeMap remainingCard = new RenegadeMap();
        harness.setLibrary(player1, List.of(artifact, remainingCard));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gameLogContains("reveals Implement of Examination")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find an available artifact still shuffles without taking it")
    void failingToFindStillShuffles() {
        setupAndCast();
        ImplementOfExamination artifact = new ImplementOfExamination();
        harness.setLibrary(player1, List.of(artifact));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new TrophyMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        resolveAllTriggers();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new ImplementOfExamination(), new RenegadeMap(),
                new TreasureKeeper(), new TrophyMage()));
    }
}
