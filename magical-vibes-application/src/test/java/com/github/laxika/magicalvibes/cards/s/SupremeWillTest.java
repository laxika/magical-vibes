package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupremeWillTest extends BaseCardTest {

    private void addManaFor(com.github.laxika.magicalvibes.model.Player p) {
        harness.addMana(p, ManaColor.BLUE, 3); // {2}{U}
    }

    @Nested
    @DisplayName("Mode 0: Counter target spell unless its controller pays {3}")
    class CounterMode {

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

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Llanowar Elves"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
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

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        }
    }

    @Nested
    @DisplayName("Mode 1: Look at the top four, one to hand, rest on bottom")
    class LookMode {

        @Test
        @DisplayName("Puts the chosen card in hand and the rest on the bottom of the library")
        void chosenToHandRestToBottom() {
            harness.setHand(player1, List.of(new SupremeWill()));
            addManaFor(player1);

            Card top1 = new GrizzlyBears();
            Card top2 = new LlanowarElves();
            Card top3 = new Island();
            Card top4 = new Plains();
            List<Card> deck = gd.playerDecks.get(player1.getId());
            deck.clear();
            deck.addAll(List.of(top1, top2, top3, top4)); // index 0 is the top

            harness.castInstant(player1, 0, 1, null); // mode 1, no target
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));
            // The three unchosen cards are ordered onto the bottom of the library.
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

            GameData gd = harness.getGameData();
            assertThat(gd.playerHands.get(player1.getId()))
                    .contains(top1).doesNotContain(top2, top3, top4);
            assertThat(gd.playerDecks.get(player1.getId()))
                    .hasSize(3).containsExactlyInAnyOrder(top2, top3, top4);
        }
    }
}
