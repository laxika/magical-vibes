package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeadTheStampede;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.r.Recoup;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenSunsZenith.class, LlanowarElves.class, GrizzlyBears.class, AirElemental.class,
        Plains.class, Swamp.class, GlissaTheTraitor.class, LeadTheStampede.class})
class GreenSunsZenithTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Green Sun's Zenith with X=2 puts it on the stack with correct X value")
    void castingPutsOnStackWithXValue() {
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        harness.addMana(player1, ManaColor.GREEN, 3); // {X}{G} with X=2 costs 2G+G = 3 mana

        harness.castSorcery(player1, 0, 2);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getCard().getName()).isEqualTo("Green Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Resolving — library search =====

    @Test
    @DisplayName("Resolving presents only green creatures with MV <= X")
    void resolvingPresentsOnlyEligibleGreenCreatures() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        // Library: LlanowarElves (MV 1, green), GrizzlyBears (MV 2, green), AirElemental (MV 5, blue), Plains, Swamp
        // X=2 → only green creatures with MV <= 2: LlanowarElves (1) and GrizzlyBears (2)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("X=1 excludes green creatures with MV > 1")
    void xOneExcludesHigherMVCreatures() {
        castZenith(1);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Only LlanowarElves (MV 1, green) qualifies
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("Non-green creatures are excluded even if MV matches")
    void nonGreenCreaturesAreExcluded() {
        castZenith(10);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // AirElemental (MV 5, blue) should not appear despite high X
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .doesNotContain("Air Elemental");
        // Only green creatures
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-creature cards are excluded even if green")
    void nonCreatureCardsAreExcluded() {
        castZenith(10);
        setupLibrary();
        harness.getGameData().playerDecks.get(player1.getId()).add(new LeadTheStampede());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Multicolor creature with green is included")
    void multicolorCreatureWithGreenIsIncluded() {
        castZenith(3);

        // GlissaTheTraitor: MV 3, black/green creature — should be eligible
        harness.setLibrary(player1, List.of(new GlissaTheTraitor(), new AirElemental()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Glissa, the Traitor");
    }

    @Test
    @DisplayName("Search destination is battlefield, not hand")
    void searchDestinationIsBattlefield() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    // ===== Choosing a card =====

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield")
    void choosingCreaturePutsItOntoBattlefield() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        // Card is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosenName));

        // Card is NOT in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosenName));

        // Library lost the chosen card but gained Green Sun's Zenith, which shuffles itself in
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain(chosenName)
                .contains("Green Sun's Zenith");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);

        // Awaiting state is cleared
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Shuffle into library (Zenith mechanic) =====

    @Test
    @DisplayName("Green Sun's Zenith is shuffled into library instead of going to graveyard")
    void zenithShufflesIntoLibraryInsteadOfGraveyard() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        // Zenith should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Green Sun's Zenith");

        // Zenith should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Green Sun's Zenith"));
    }

    // ===== Fail to find =====

    @Test
    @DisplayName("Search with stated qualities allows fail to find")
    void canFailToFindIsTrue() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Player can fail to find by choosing index -1")
    void failToFindShufflesLibrary() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        // No card added to hand or battlefield
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        // Awaiting state is cleared
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== No eligible creatures =====

    @Test
    @DisplayName("When no green creatures with MV <= X exist, shuffles library and logs")
    void noEligibleCreaturesShufflesAndLogs() {
        castZenith(0);
        setupLibrary(); // Lowest MV green creature is LlanowarElves at MV 1

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no green creature card with mana value"));
    }

    @Test
    @DisplayName("When library has only non-green creatures, shuffles and logs")
    void onlyNonGreenCreaturesInLibrary() {
        castZenith(10);

        harness.setLibrary(player1, List.of(new AirElemental(), new Plains()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no green creature card with mana value"));
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with empty library logs and does not crash")
    void emptyLibrary() {
        castZenith(3);

        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("it is empty"));
    }

    @Test
    @DisplayName("Zenith stays out of the library until the search choice is completed")
    void staysOutOfLibraryDuringSearch() {
        GreenSunsZenith zenith = new GreenSunsZenith();
        harness.setHand(player1, List.of(zenith));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(zenith);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zenith);
    }

    @Test
    @CardUsed({Twincast.class, CosisTrickster.class})
    @DisplayName("A resolving copy performs both shuffles and does not put a copy in the library")
    void copiedZenithTriggersTwoShuffles() {
        GreenSunsZenith zenith = new GreenSunsZenith();
        harness.setHand(player1, List.of(zenith, new Twincast(), new Twincast()));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addToBattlefield(player2, new CosisTrickster());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 1);
        harness.castAndResolveInstant(player1, 0, zenith.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof CosisTrickster)).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @CardUsed(Recoup.class)
    @DisplayName("Flashback exiles Zenith instead of putting it into its owner's library")
    void flashbackExilesZenithWithoutLeavingItInLibrary() {
        GreenSunsZenith zenith = new GreenSunsZenith();
        harness.setGraveyard(player1, List.of(zenith));
        harness.setHand(player1, List.of(new Recoup()));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, zenith.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFlashback(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zenith);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(zenith);
        harness.assertNotInGraveyard(player1, "Green Sun's Zenith");
    }

    @Test
    @CardUsed(CosisTrickster.class)
    @DisplayName("A non-owner controller performs both shuffles, including the owner's library")
    void nonOwnerControllerPerformsBothShuffles() {
        GreenSunsZenith zenith = new GreenSunsZenith();
        zenith.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(zenith));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new CosisTrickster());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zenith);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof CosisTrickster)).hasSize(2);
    }

    private void castZenith(int xValue) {
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        // {X}{G} costs X generic + 1 green
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setupLibrary() {
        // LlanowarElves: MV 1 (green creature), GrizzlyBears: MV 2 (green creature),
        // AirElemental: MV 5 (blue creature), Plains: MV 0 (basic land), Swamp: MV 0 (basic land)
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears(), new AirElemental(), new Plains(), new Swamp()));
    }
}
