package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrypticSerpentTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Can cast for full cost {5}{U}{U} with empty graveyard")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new CrypticSerpent()));
            harness.addMana(player1, ManaColor.BLUE, 7);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast for full cost with insufficient mana")
        void cannotCastWithInsufficientMana() {
            harness.setHand(player1, List.of(new CrypticSerpent()));
            harness.addMana(player1, ManaColor.BLUE, 6);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Cost is reduced by 1 for each instant and sorcery card in graveyard")
        void costReducedByInstantAndSorceryCards() {
            harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe(), new Shock()));
            harness.setHand(player1, List.of(new CrypticSerpent()));
            // 3 instant/sorcery cards = reduce by 3, so {2}{U}{U} = 4 mana
            harness.addMana(player1, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Non-instant/sorcery cards in graveyard do not reduce the cost")
        void nonInstantSorceryCardsDoNotReduceCost() {
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
            harness.setHand(player1, List.of(new CrypticSerpent()));
            // Only creatures in graveyard — no reduction, still {5}{U}{U} = 7 mana
            harness.addMana(player1, ManaColor.BLUE, 6);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Only instant and sorcery cards count when graveyard is mixed")
        void onlyInstantAndSorceryCardsCount() {
            harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears(), new LavaAxe()));
            harness.setHand(player1, List.of(new CrypticSerpent()));
            // 2 instant/sorcery cards = reduce by 2, so {3}{U}{U} = 5 mana
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost cannot be reduced below the colored mana requirement {U}{U}")
        void costCannotGoBelowColoredMana() {
            // 9 instant/sorcery cards would reduce generic cost below 0, but it floors leaving {U}{U}
            harness.setGraveyard(player1, List.of(
                    new Shock(), new Shock(), new Shock(),
                    new Shock(), new Shock(), new Shock(),
                    new Shock(), new Shock(), new Shock()));
            harness.setHand(player1, List.of(new CrypticSerpent()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's graveyard instants and sorceries do not reduce the cost")
        void opponentGraveyardDoesNotReduceCost() {
            harness.setGraveyard(player2, List.of(new Shock(), new LavaAxe(), new Shock()));
            harness.setHand(player1, List.of(new CrypticSerpent()));
            // Opponent's graveyard should not help — still {5}{U}{U} = 7 mana
            harness.addMana(player1, ManaColor.BLUE, 6);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }
}
