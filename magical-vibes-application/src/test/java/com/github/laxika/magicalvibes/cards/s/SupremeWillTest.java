package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupremeWill.class, GrizzlyBears.class, LlanowarElves.class, Island.class, Plains.class})
class SupremeWillTest extends BaseCardTest {

    private void addManaFor(com.github.laxika.magicalvibes.model.Player p) {
        harness.addMana(p, ManaColor.BLUE, 3); // {2}{U}
    }

    @Nested
    @DisplayName("Mode 0: Counter target spell unless its controller pays {3}")
    @CardUsed({SupremeWill.class, LlanowarElves.class, Island.class})
    class CounterMode {

        @Test
        void countersWhenControllerDeclinesAffordablePayment() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player2, List.of(elves));
            harness.addMana(player2, ManaColor.GREEN, 4);
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            harness.castCreature(player2, 0);
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, false);

            harness.assertInGraveyard(player2, "Llanowar Elves");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            harness.assertInGraveyard(player1, "Supreme Will");
        }

        @Test
        @CardUsed({SupremeWill.class})
        void countersAnInstantWithoutResolvingItsLookMode() {
            harness.forceActivePlayer(player2);
            SupremeWill opposingSpell = new SupremeWill();
            harness.setHand(player2, List.of(opposingSpell));
            harness.addMana(player2, ManaColor.BLUE, 3);
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            harness.castInstant(player2, 0, 1, null);
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, opposingSpell.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Supreme Will");
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @CardUsed({SupremeWill.class, Island.class})
        void offersPaymentWhenManaCanBeProducedDuringResolution() {
            harness.forceActivePlayer(player2);
            SupremeWill opposingSpell = new SupremeWill();
            harness.setHand(player2, List.of(opposingSpell));
            harness.addMana(player2, ManaColor.BLUE, 3);
            harness.addToBattlefield(player2, new Island());
            harness.addToBattlefield(player2, new Island());
            harness.addToBattlefield(player2, new Island());
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            harness.castInstant(player2, 0, 1, null);
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, opposingSpell.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player2, "Supreme Will");
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }

        @Test
        @DisplayName("Counters when opponent cannot pay {3}")
        void countersWhenCannotPay() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player2, List.of(elves));
            harness.addMana(player2, ManaColor.GREEN, 1); // only enough to cast

            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            harness.castCreature(player2, 0);
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Llanowar Elves");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        }

        @Test
        @DisplayName("Not countered when opponent pays {3}")
        void notCounteredWhenPays() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player2, List.of(elves));
            harness.addMana(player2, ManaColor.GREEN, 4); // 1 to cast, 3 to pay

            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            harness.castCreature(player2, 0);
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player2, true);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Llanowar Elves");
        }
    }

    @Nested
    @DisplayName("Mode 1: Look at the top four, one to hand, rest on bottom")
    @CardUsed({SupremeWill.class, GrizzlyBears.class, LlanowarElves.class, Island.class, Plains.class})
    class LookMode {

        @Test
        @CardUsed({SupremeWill.class, Island.class, Plains.class})
        void preservesUnseenCardsAndHonorsChosenBottomOrder() {
            Card first = new Island();
            Card second = new Plains();
            Card third = new Island();
            Card fourth = new Plains();
            Card unseen = new Island();
            harness.setLibrary(player1, List.of(first, second, third, fourth, unseen));
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);
            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.allCards()).containsExactly(first, second, third, fourth);
            harness.handleMultipleCardsChosen(player1, List.of(third.getId()));
            List<Card> reorder =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                    List.of(reorder.indexOf(fourth), reorder.indexOf(second), reorder.indexOf(first))));

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, fourth, second, first);
            assertThat(gd.interaction.activeInteraction()).isNull();
            harness.assertInGraveyard(player1, "Supreme Will");
        }

        @Test
        @CardUsed({SupremeWill.class, Island.class, Plains.class})
        void worksWithTwoCardsInLibrary() {
            Card chosen = new Island();
            Card remaining = new Plains();
            harness.setLibrary(player1, List.of(chosen, remaining));
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);
            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @CardUsed({SupremeWill.class, Island.class})
        void putsOnlyCardInHandWithoutDrawing() {
            Card onlyCard = new Island();
            harness.setLibrary(player1, List.of(onlyCard));
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);
            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            harness.assertInGraveyard(player1, "Supreme Will");
        }

        @Test
        @CardUsed({SupremeWill.class})
        void resolvesWithEmptyLibraryWithoutDrawingOrChoosing() {
            harness.setLibrary(player1, List.of());
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);
            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            harness.assertInGraveyard(player1, "Supreme Will");
        }

        @Test
        @CardUsed({SupremeWill.class, Island.class, Plains.class})
        void cannotDeclineMandatoryCardSelection() {
            Card first = new Island();
            Card second = new Plains();
            harness.setLibrary(player1, List.of(first, second));
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);
            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThatThrownBy(
                    () -> harness.handleMultipleCardsChosen(player1, List.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid number of cards selected");
            harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        }

        @Test
        @DisplayName("Puts the chosen card in hand and the rest on the bottom of the library")
        void chosenToHandRestToBottom() {
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            Card top1 = new GrizzlyBears();
            Card top2 = new LlanowarElves();
            Card top3 = new Island();
            Card top4 = new Plains();
            harness.setLibrary(player1, List.of(top1, top2, top3, top4));

            harness.castInstant(player1, 0, 1, null); // mode 1, no target
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));
            // The three unchosen cards are ordered onto the bottom of the library.
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

            assertThat(gd.playerHands.get(player1.getId()))
                    .contains(top1).doesNotContain(top2, top3, top4);
            assertThat(gd.playerDecks.get(player1.getId()))
                    .hasSize(3).containsExactlyInAnyOrder(top2, top3, top4);
        }
    }
}
