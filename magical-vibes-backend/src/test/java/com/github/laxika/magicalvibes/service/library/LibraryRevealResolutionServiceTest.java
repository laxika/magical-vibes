package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.cards.a.AvenWindreader;
import com.github.laxika.magicalvibes.cards.c.CloneShell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SageOwl;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryRevealResolutionServiceTest extends BaseCardTest {

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // =========================================================================
    // resolveRevealTopCardOfLibrary (via AvenWindreader)
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealTopCardOfLibrary")
    class ResolveRevealTopCardOfLibrary {

        @Test
        @DisplayName("Reveals top card name in log")
        void revealsTopCard() {
            Permanent windreader = addReadyPermanent(player1, new AvenWindreader());
            harness.addMana(player1, ManaColor.BLUE, 2);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            deck.clear();
            Card topCard = new GrizzlyBears();
            deck.add(topCard);

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("reveals") && log.contains("Grizzly Bears"));
        }

        @Test
        @DisplayName("Empty library logs appropriately")
        void emptyLibraryLogged() {
            Permanent windreader = addReadyPermanent(player1, new AvenWindreader());
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player2.getId()).clear();

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }
    }

    // =========================================================================
    // resolveReorderTopCardsOfLibrary (via SageOwl)
    // =========================================================================

    @Nested
    @DisplayName("resolveReorderTopCardsOfLibrary")
    class ResolveReorderTopCardsOfLibrary {

        @Test
        @DisplayName("Empty library skips reorder")
        void emptyLibrarySkipsReorder() {
            harness.setHand(player1, List.of(new SageOwl()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }

        @Test
        @DisplayName("Single card skips reorder prompt")
        void singleCardSkipsReorder() {
            harness.setHand(player1, List.of(new SageOwl()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_REORDER state")
        void multipleCardsEntersReorderState() {
            harness.setHand(player1, List.of(new SageOwl()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
            assertThat(gd.interaction.libraryView().reorderPlayerId()).isEqualTo(player1.getId());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsHandTopBottom (via TellingTime)
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsHandTopBottom")
    class ResolveLookAtTopCardsHandTopBottom {

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            harness.setHand(player1, List.of(new TellingTime()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }

        @Test
        @DisplayName("Single card automatically goes to hand")
        void singleCardAutoToHand() {
            harness.setHand(player1, List.of(new TellingTime()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            gd.playerDecks.get(player1.getId()).clear();
            Card singleCard = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).add(singleCard);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Multiple cards enters HAND_TOP_BOTTOM_CHOICE state")
        void multipleCardsEntersChoice() {
            harness.setHand(player1, List.of(new TellingTime()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
            assertThat(gd.interaction.libraryView().handTopBottomPlayerId()).isEqualTo(player1.getId());
            assertThat(gd.interaction.libraryView().handTopBottomCards()).hasSize(3);
        }
    }

    // =========================================================================
    // resolveImprintFromTopCards (via CloneShell ETB)
    // =========================================================================

    @Nested
    @DisplayName("resolveImprintFromTopCards")
    class ResolveImprintFromTopCards {

        @Test
        @DisplayName("Empty library logs and does nothing")
        void emptyLibraryLogs() {
            harness.setHand(player1, List.of(new CloneShell()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }

        @Test
        @DisplayName("Single card is automatically exiled face down")
        void singleCardAutoExiled() {
            harness.setHand(player1, List.of(new CloneShell()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);

            gd.playerDecks.get(player1.getId()).clear();
            Card singleCard = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).add(singleCard);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerExiledCards.get(player1.getId())).contains(singleCard);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles a card face down"));
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_SEARCH state for exile choice")
        void multipleCardsEntersSearchState() {
            harness.setHand(player1, List.of(new CloneShell()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        }
    }
}
