package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.t.TransguildPromenade;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GatecreeperVine.class, Forest.class, RakdosGuildgate.class, TransguildPromenade.class})
class GatecreeperVineTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability offers only basic lands and Gates")
    void searchOffersBasicLandsAndGates() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Rakdos Guildgate");
    }

    @Test
    @DisplayName("Choosing a Gate puts it into hand")
    void choosingGatePutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        int gateIndex = offered.indexOf(offered.stream()
                .filter(c -> c.getName().equals("Rakdos Guildgate")).findFirst().orElseThrow());

        harness.handleCardChosen(player1, gateIndex);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Rakdos Guildgate"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the search")
    void decliningSkipsSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(e -> e.contains("searches their library"));
    }

    @Test
    @DisplayName("Choosing a basic land reveals it, moves it to hand, and shuffles")
    void choosingBasicLandRevealsAndShuffles() {
        setupAndCast();
        Forest forest = new Forest();
        RakdosGuildgate gate = new RakdosGuildgate();
        harness.setLibrary(player1, List.of(forest, gate));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(gate);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(forest);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(e -> e.contains("reveals Forest"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(e -> e.toLowerCase().contains("library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search may fail to find even when matching cards exist")
    void mayFailToFindWithMatchingCards() {
        setupAndCast();
        Forest forest = new Forest();
        RakdosGuildgate gate = new RakdosGuildgate();
        harness.setLibrary(player1, List.of(forest, gate));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, gate);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(e -> e.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nonbasic lands without the Gate subtype cannot be found")
    void nonbasicNonGateLandCannotBeFound() {
        setupAndCast();
        TransguildPromenade land = new TransguildPromenade();
        harness.setLibrary(player1, List.of(land));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(e -> e.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with an empty library finishes and shuffles")
    void emptyLibrarySearchFinishes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(e -> e.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GatecreeperVine()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new RakdosGuildgate(), new GatecreeperVine()));
    }
}
