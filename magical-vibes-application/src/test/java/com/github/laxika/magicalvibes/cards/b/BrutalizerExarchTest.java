package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShrineOfBurningRage;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrutalizerExarch.class, GlistenerElf.class, Plains.class, Island.class, Forest.class, ShrineOfBurningRage.class})
class BrutalizerExarchTest extends BaseCardTest {

    

    @CardUsed({BrutalizerExarch.class, GlistenerElf.class, Plains.class, Island.class, Forest.class, ShrineOfBurningRage.class})
    @Nested
    @DisplayName("Mode 1: Search library for creature to top")
    class SearchMode {

        @Test
        @DisplayName("Choosing mode 1 triggers library search for creature cards")
        void mode1TriggersLibrarySearch() {
            setupLibraryWithCreatures();
            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            harness.assertOnBattlefield(player1, "Brutalizer Exarch");
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            // Only creature cards should be shown
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .allMatch(c -> c.hasType(CardType.CREATURE));
        }

        @Test
        @DisplayName("Choosing a creature puts it on top of library")
        void choosingCreaturePutsOnTop() {
            setupLibraryWithCreatures();
            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
            String chosenName = offered.getFirst().getName();

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            // The chosen card should be on top of the library
            List<Card> deck = gd.playerDecks.get(player1.getId());
            assertThat(deck).isNotEmpty();
            assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        }

        @Test
        @DisplayName("Failing to find is allowed")
        void failToFindIsAllowed() {
            setupLibraryWithCreatures();
            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            // -1 means fail to find
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

            // Deck should be shuffled but same size (no card moved)
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Only creature cards are shown in library search")
        void onlyCreaturesShown() {
            harness.setLibrary(player1, List.of(new Plains(), new Island(), new Forest()));

            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // No creatures in library, so search finds nothing
            GameData gd = harness.getGameData();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }
    }

    @CardUsed({BrutalizerExarch.class, GlistenerElf.class, Plains.class, Island.class, Forest.class, ShrineOfBurningRage.class})
    @Nested
    @DisplayName("Mode 2: Put noncreature permanent on bottom")
    class BottomMode {

        @Test
        @DisplayName("Choosing mode 2 puts target noncreature permanent on bottom of owner's library")
        void mode2PutsNoncreaturePermanentOnBottom() {
            // Put a noncreature permanent onto the battlefield
            harness.addToBattlefield(player2, new Plains());
            UUID targetId = harness.getPermanentId(player2, "Plains");

            castWithMode2(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            // The Plains should no longer be on the battlefield
            harness.assertNotOnBattlefield(player2, "Plains");
            // It should be on the bottom of the owner's library
            List<Card> deck = gd.playerDecks.get(player2.getId());
            assertThat(deck.getLast().getName()).isEqualTo("Plains");
        }

        @Test
        @DisplayName("Mode 2 works on artifacts")
        void mode2WorksOnArtifacts() {
            harness.addToBattlefield(player2, new ShrineOfBurningRage());
            UUID targetId = harness.getPermanentId(player2, "Shrine of Burning Rage");

            castWithMode2(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertNotOnBattlefield(player2, "Shrine of Burning Rage");
        }

        @Test
        @DisplayName("Brutalizer Exarch enters the battlefield with mode 2")
        void exarchEntersBattlefield() {
            harness.addToBattlefield(player2, new Plains());
            UUID targetId = harness.getPermanentId(player2, "Plains");

            castWithMode2(targetId);
            harness.passBothPriorities(); // resolve creature

            harness.assertOnBattlefield(player1, "Brutalizer Exarch");
        }
    }

    @Test
    @DisplayName("Mode is chosen when the ETB trigger is put on the stack")
    void choosesModeAfterEnteringBattlefield() {
        setupLibraryWithCreatures();
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new BrutalizerExarch()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brutalizer Exarch");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Searching an empty library completes without a choice")
    void emptyLibrarySearchCompletes() {
        harness.setLibrary(player1, List.of());
        castWithMode1();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Brutalizer Exarch");
    }

    @Test
    @DisplayName("The bottom mode can target a permanent you control")
    void canPutOwnPermanentOnBottom() {
        Plains plains = new Plains();
        harness.addToBattlefield(player1, plains);
        castWithMode2(harness.getPermanentId(player1, "Plains"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(plains);
    }

    @Test
    @DisplayName("A creature found in a one-card library is revealed and remains on top")
    void revealsCreatureFromSingleCardLibrary() {
        GlistenerElf creature = new GlistenerElf();
        harness.setLibrary(player1, List.of(creature));
        castWithMode1();
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals Glistener Elf"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A target that leaves before resolution is not put into the library")
    void missingTargetDoesNotMoveAnotherPermanent() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Plains");
        castWithMode2(targetId);
        harness.passBothPriorities();
        var target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId)).findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToLibraryBottom(gd, target);
        List<Card> libraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(libraryBefore);
    }

    private void castWithMode1() {
        harness.setHand(player1, List.of(new BrutalizerExarch()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 0); // mode 0 = search library
    }

    private void castWithMode2(UUID targetId) {
        harness.setHand(player1, List.of(new BrutalizerExarch()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 1, targetId); // mode 1 = put on bottom
    }

    private void setupLibraryWithCreatures() {
        harness.setLibrary(player1, List.of(new GlistenerElf(), new Plains(), new Island(), new Forest()));
    }
}
