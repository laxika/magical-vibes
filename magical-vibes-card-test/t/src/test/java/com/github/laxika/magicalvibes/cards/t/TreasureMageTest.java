package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.p.PeaceStrider;
import com.github.laxika.magicalvibes.cards.m.MassacreWurm;
import com.github.laxika.magicalvibes.cards.p.PhyrexianJuggernaut;
import com.github.laxika.magicalvibes.cards.s.SpineOfIshSah;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureMage.class, PeaceStrider.class, MassacreWurm.class, PhyrexianJuggernaut.class, SpineOfIshSah.class})
class TreasureMageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Treasure Mage creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Treasure Mage");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability presents only artifacts with MV 6 or greater")
    void acceptingMayPresentsOnlyHighMVArtifacts() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // PhyrexianJuggernaut (MV 6) and SpineOfIshSah (MV 7) should be offered
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.ARTIFACT)
                        && c.getManaValue() >= 6);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand")
    void choosingArtifactPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Declining may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Artifacts with MV 5 or less are excluded from search")
    void lowMVArtifactsExcluded() {
        setupAndCast();
        // Library with only low-MV artifacts and a creature
        harness.setLibrary(player1, List.of(new PeaceStrider(), new MassacreWurm()));

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no artifact cards with mana value 6 or greater"));
    }

    @Test
    @DisplayName("Non-artifact cards are excluded from search even if high MV")
    void nonArtifactsExcluded() {
        setupAndCast();
        // Library with only non-artifact cards
        harness.setLibrary(player1, List.of(new MassacreWurm(), new MassacreWurm()));

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no artifact cards with mana value 6 or greater"));
    }

    @Test
    @DisplayName("Player can fail to find with Treasure Mage")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // Resolve the optional enter ability.
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library finishes without taking a card")
    void emptyLibraryFinishesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("A noncreature artifact above six mana is revealed and moved only to the controller's hand")
    void findsNoncreatureArtifactAboveSixMana() {
        setupAndCast();
        SpineOfIshSah artifact = new SpineOfIshSah();
        harness.setLibrary(player1, List.of(artifact, new PeaceStrider()));
        harness.setLibrary(player2, List.of(new MassacreWurm()));
        int opponentHandSize = harness.getGameData().playerHands.get(player2.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).doesNotContain(artifact);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals") && entry.contains("Spine of Ish Sah"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new TreasureMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new PhyrexianJuggernaut(), new SpineOfIshSah(), new PeaceStrider(), new MassacreWurm()));
    }
}
