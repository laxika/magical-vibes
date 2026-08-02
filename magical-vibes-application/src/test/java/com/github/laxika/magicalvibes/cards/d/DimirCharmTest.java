package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DimirCharmTest extends BaseCardTest {

    private void addUB(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    @Nested
    @DisplayName("Mode 0: Counter target sorcery spell")
    class CounterMode {

        @Test
        @DisplayName("Counters a sorcery spell")
        void countersSorcery() {
            Divination divination = new Divination();
            harness.setHand(player1, List.of(divination));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new DimirCharm()));
            addUB(player2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, divination.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Divination");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a non-sorcery spell")
        void cannotTargetNonSorcery() {
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new DimirCharm()));
            addUB(player2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, elves.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target creature with power 2 or less")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a creature with power 2")
        void destroysSmallCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new DimirCharm()));
            addUB(player1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a creature with power 3")
        void cannotTargetBigCreature() {
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new DimirCharm()));
            addUB(player1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Hill Giant")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Look at the top three cards of target player's library")
    class MillMode {

        @Test
        @DisplayName("Chosen card stays on top and the rest go to that player's graveyard")
        void keepsOneOnTopAndMillsRest() {
            Card c0 = new Island();
            Card c1 = new Forest();
            Card c2 = new GrizzlyBears();
            Card c3 = new Mountain();
            setDeck(player2, List.of(c0, c1, c2, c3));

            harness.setHand(player1, List.of(new DimirCharm()));
            addUB(player1);

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

            // The spell's controller chooses which card stays on top.
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

            assertThat(gd.playerDecks.get(player2.getId()))
                    .extracting(Card::getId)
                    .containsExactly(c1.getId(), c3.getId());
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .extracting(Card::getId)
                    .containsExactlyInAnyOrder(c0.getId(), c2.getId());
        }

        @Test
        @DisplayName("Can target its own controller")
        void canTargetController() {
            Card c0 = new Island();
            Card c1 = new Forest();
            Card c2 = new Mountain();
            setDeck(player1, List.of(c0, c1, c2));

            harness.setHand(player1, List.of(new DimirCharm()));
            addUB(player1);

            harness.castInstant(player1, 0, 2, player1.getId());
            harness.passBothPriorities();

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));

            assertThat(gd.playerDecks.get(player1.getId()))
                    .extracting(Card::getId)
                    .containsExactly(c2.getId());
            // Dimir Charm itself is in the same graveyard after resolving.
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .extracting(Card::getId)
                    .contains(c0.getId(), c1.getId());
        }

        @Test
        @DisplayName("Resolving against an empty library does nothing")
        void emptyLibraryDoesNothing() {
            setDeck(player2, List.of());

            harness.setHand(player1, List.of(new DimirCharm()));
            addUB(player1);

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }
    }
}
