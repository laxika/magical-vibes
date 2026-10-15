package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.ManaforceMace;
import com.github.laxika.magicalvibes.cards.p.ParagonOfTheAmesha;
import com.github.laxika.magicalvibes.cards.p.ParasiticStrix;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxSummoner.class, ParasiticStrix.class, ManaforceMace.class, ParagonOfTheAmesha.class})
class SphinxSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sphinx Summoner creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sphinx Summoner");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability presents only artifact creatures")
    void acceptingMayPresentsOnlyArtifactCreatures() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(c -> c.getName())
                .containsExactly("Parasitic Strix");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact creature puts it into hand")
    void choosingArtifactCreaturePutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
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
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Non-creature artifacts and non-artifact creatures are excluded from search")
    void nonMatchingCardsExcluded() {
        setupAndCast();
        // Library with only a non-creature artifact and a non-artifact creature
        harness.setLibrary(player1, List.of(new ManaforceMace(), new ParagonOfTheAmesha()));

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no artifact creature cards"));
    }

    @Test
    @DisplayName("Player can fail to find with Sphinx Summoner")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepted search of an empty library completes and shuffles")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("library but it is empty") && entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Search reveals and takes only one of multiple matching cards")
    void takesOnlyOneMatchingCard() {
        setupAndCast();
        ParasiticStrix chosen = new ParasiticStrix();
        SphinxSummoner remaining = new SphinxSummoner();
        harness.setLibrary(player1, List.of(chosen, remaining));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Parasitic Strix"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SphinxSummoner()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new ParasiticStrix(), new ManaforceMace(), new ParagonOfTheAmesha()));
    }
}
