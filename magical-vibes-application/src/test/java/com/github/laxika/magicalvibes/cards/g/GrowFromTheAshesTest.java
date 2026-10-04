package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MemorialToFolly;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowFromTheAshes.class, Plains.class, Forest.class, Island.class, Mountain.class, MemorialToFolly.class})
class GrowFromTheAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        setupAndCast();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(GrowFromTheAshes.class);
    }

    @Test
    @DisplayName("Without kicker — presents basic lands with destination battlefield (untapped)")
    void resolvingPresentsBasicLandsToBattlefield() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Without kicker — chosen basic land enters the battlefield untapped")
    void chosenBasicLandEntersUntapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without kicker — player can fail to find")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With kicker — initiates multi-pick search for two basic lands")
    void kickedSearchesForTwoBasicLands() {
        setupAndCastKicked();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("With kicker — two basic lands enter the battlefield untapped")
    void kickedPutsTwoLandsOntoBattlefield() {
        setupAndCastKicked();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Choose first land
        harness.handleCardChosen(player1, 0);
        // Choose second land
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        long untappedLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped())
                .count();
        assertThat(untappedLands).isGreaterThanOrEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolving with no basic lands in library does not prompt for library choice")
    void noBasicLandsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GrowFromTheAshes(), new GrowFromTheAshes()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Resolving with empty library does not prompt for library choice")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("it is empty"));
    }

    @Test
    @DisplayName("Kicker adds two mana to the normal casting cost")
    void kickerPaysTwoAdditionalMana() {
        setupAndCastKicked();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        setupLibrary();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("Search excludes nonbasic lands and nonland cards")
    void excludesNonbasicLandsAndNonlands() {
        setupAndCast();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new MemorialToFolly(), new GrowFromTheAshes(), forest));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicked search puts both lands onto the battlefield only after both are selected")
    void kickedLandsEnterTogether() {
        setupAndCastKicked();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicked search can stop after finding one basic land")
    void kickedCanFindOnlyOneLand() {
        setupAndCastKicked();
        setupLibrary();
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicked search completes when only one basic land is available")
    void kickedWithOnlyOneAvailableLand() {
        setupAndCastKicked();
        harness.setLibrary(player1, List.of(new Forest(), new GrowFromTheAshes()));
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicked search can fail to find both lands")
    void kickedCanFailToFindAllLands() {
        setupAndCastKicked();
        setupLibrary();
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GrowFromTheAshes()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupAndCastKicked() {
        harness.setHand(player1, List.of(new GrowFromTheAshes()));
        harness.addMana(player1, ManaColor.GREEN, 5); // {2}{G} + kicker {2}
        harness.castKickedSorcery(player1, 0, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new Mountain()));
    }
}
