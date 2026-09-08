package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChitinGravestalkerTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Can cast for full cost with no matching graveyard cards")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new ChitinGravestalker()));
            harness.addMana(player1, ManaColor.BLACK, 6);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Costs one less for each artifact or creature card in the controller's graveyard")
        void costsOneLessForEachArtifactOrCreatureCard() {
            harness.setGraveyard(player1, List.of(new Ornithopter(), new GrizzlyBears(), new Shock()));
            harness.setHand(player1, List.of(new ChitinGravestalker()));
            harness.addMana(player1, ManaColor.BLACK, 4);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Opponent graveyard and non-artifact noncreature cards do not reduce the cost")
        void ignoresOpponentAndNonmatchingCards() {
            harness.setGraveyard(player1, List.of(new Shock()));
            harness.setGraveyard(player2, List.of(new Ornithopter(), new GrizzlyBears()));
            harness.setHand(player1, List.of(new ChitinGravestalker()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Cost reduction floors the generic cost at zero")
        void costReductionFloorsAtZero() {
            harness.setGraveyard(player1, List.of(
                    new Ornithopter(), new Ornithopter(), new Ornithopter(),
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            harness.setHand(player1, List.of(new ChitinGravestalker()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }
    }

    @Test
    @DisplayName("Cycling discards Chitin Gravestalker and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ChitinGravestalker()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Chitin Gravestalker");
        harness.assertInHand(player1, "Island");
    }
}
