package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.NissaVastwoodSeer;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
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

@CardUsed({WoodlandBellower.class, FugitiveWizard.class, GiantSpider.class, GrizzlyBears.class,
        LlanowarElves.class, NissaVastwoodSeer.class, HitchclawRecluse.class, TitanicGrowth.class})
class WoodlandBellowerTest extends BaseCardTest {

    @Test
    @DisplayName("Search offers only nonlegendary green creatures with mana value 3 or less")
    void searchOffersOnlyMatchingCreatures() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
    }

    @Test
    @DisplayName("Chosen creature is put onto the battlefield")
    void chosenCreatureEntersBattlefield() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        int index = offered.indexOf(offered.stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).findFirst().orElseThrow());

        harness.handleCardChosen(player1, index);

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
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
    @DisplayName("A creature with mana value exactly three is eligible, but a green instant is not")
    void searchIncludesManaValueThreeAndExcludesGreenNoncreatures() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new HitchclawRecluse(), new TitanicGrowth()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Hitchclaw Recluse");
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Hitchclaw Recluse").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Hitchclaw Recluse");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Titanic Growth");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(text -> text.toLowerCase().contains("shuffl"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An accepted search may fail to find an eligible creature and still shuffles")
    void acceptedSearchMayFailToFind() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new HitchclawRecluse()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Hitchclaw Recluse");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Hitchclaw Recluse");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(text -> text.toLowerCase().contains("shuffl"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting a search with no matching cards still shuffles and finishes")
    void noMatchingCardsStillShuffles() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new TitanicGrowth(), new NissaVastwoodSeer()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Titanic Growth", "Nissa, Vastwood Seer");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(text -> text.toLowerCase().contains("shuffl"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Nissa, Vastwood Seer");
    }

    @Test
    @DisplayName("An accepted search of an empty library finishes without a choice")
    void emptyLibrarySearchFinishes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(text -> text.toLowerCase().contains("shuffl"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Woodland Bellower");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new WoodlandBellower()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new NissaVastwoodSeer(),
                new GiantSpider(), new FugitiveWizard()));
    }
}
