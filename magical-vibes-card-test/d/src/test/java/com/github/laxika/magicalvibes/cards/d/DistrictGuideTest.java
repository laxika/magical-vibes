package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DistrictGuide.class, Forest.class, RakdosGuildgate.class, GrizzlyBears.class, TempleGarden.class})
class DistrictGuideTest extends BaseCardTest {

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Choosing a basic land reveals it, puts it into hand, and shuffles")
    void choosingBasicLandRevealsItAndShuffles() {
        setupAndCast();
        Forest forest = new Forest();
        TempleGarden nonbasic = new TempleGarden();
        harness.setLibrary(player1, List.of(forest, nonbasic));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Forest") && entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("A nonbasic Forest is not offered by the search")
    void nonbasicLandWithBasicLandTypesIsNotOffered() {
        setupAndCast();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new TempleGarden(), forest));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(forest);
    }

    @Test
    @DisplayName("An accepted search may find no card even with matching cards")
    void mayFailToFindWithMatchingCards() {
        setupAndCast();
        setupLibrary();
        List<Card> originalLibrary = List.copyOf(harness.getGameData().playerDecks.get(player1.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("chooses not to take a card") && entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("An accepted search with no matches finishes and shuffles")
    void noMatchingCardsFinishesAndShuffles() {
        setupAndCast();
        TempleGarden nonbasic = new TempleGarden();
        harness.setLibrary(player1, List.of(nonbasic));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("searches their library") && entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("An accepted search of an empty library finishes normally")
    void emptyLibraryFinishesNormally() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("library but it is empty") && entry.contains("Library is shuffled"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new DistrictGuide()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new RakdosGuildgate(), new GrizzlyBears()));
    }
}
