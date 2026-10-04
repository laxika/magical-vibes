package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TrackersInstincts;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ghoultree.class, TrackersInstincts.class})
class GhoultreeTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({Ghoultree.class, TrackersInstincts.class})
    class CostReduction {

        @Test
        @DisplayName("Generic reduction cannot pay the green mana requirement")
        void stillRequiresGreenMana() {
            harness.setGraveyard(player1, List.of(
                    new Ghoultree(), new Ghoultree(), new Ghoultree(),
                    new Ghoultree(), new Ghoultree(), new Ghoultree(),
                    new Ghoultree(), new Ghoultree()));
            harness.setHand(player1, List.of(new Ghoultree()));
            harness.addMana(player1, ManaColor.COLORLESS, 8);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(8);
        }

        @Test
        @DisplayName("Reduction uses the graveyard at the time of casting")
        void reductionUpdatesWhenGraveyardChanges() {
            harness.setGraveyard(player1, List.of(new Ghoultree(), new Ghoultree(), new Ghoultree()));
            harness.setHand(player1, List.of(new Ghoultree()));
            harness.addMana(player1, ManaColor.GREEN, 5);
            harness.setGraveyard(player1, List.of(new Ghoultree(), new Ghoultree()));

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");

            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Can cast for full cost {7}{G} with empty graveyard")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new Ghoultree()));
            harness.addMana(player1, ManaColor.GREEN, 8);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(entry.getCard().getName()).isEqualTo("Ghoultree");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast for full cost with insufficient mana")
        void cannotCastWithInsufficientMana() {
            harness.setHand(player1, List.of(new Ghoultree()));
            harness.addMana(player1, ManaColor.GREEN, 7);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Cost is reduced by 1 for each creature card in graveyard")
        void costReducedByCreatureCardsInGraveyard() {
            harness.setGraveyard(player1, List.of(new Ghoultree(), new Ghoultree(), new Ghoultree()));
            harness.setHand(player1, List.of(new Ghoultree()));
            // 3 creature cards = cost reduced by 3, so {4}{G} = 5 mana
            harness.addMana(player1, ManaColor.GREEN, 5);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ghoultree");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Non-creature cards in graveyard do not reduce the cost")
        void nonCreatureCardsDoNotReduceCost() {
            harness.setGraveyard(player1, List.of(new TrackersInstincts(), new TrackersInstincts()));
            harness.setHand(player1, List.of(new Ghoultree()));
            // Only sorceries in graveyard — no reduction, still {7}{G} = 8 mana
            harness.addMana(player1, ManaColor.GREEN, 7);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Only creature cards count when graveyard is mixed")
        void onlyCreatureCardsCount() {
            harness.setGraveyard(player1, List.of(new Ghoultree(), new TrackersInstincts(), new Ghoultree()));
            harness.setHand(player1, List.of(new Ghoultree()));
            // 2 creature cards = cost reduced by 2, so {5}{G} = 6 mana
            harness.addMana(player1, ManaColor.GREEN, 6);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost cannot be reduced below the colored mana requirement {G}")
        void costCannotGoBelowColoredMana() {
            // 9 creature cards would reduce generic cost below 0, but it floors at 0 leaving {G}
            harness.setGraveyard(player1, List.of(
                    new Ghoultree(), new Ghoultree(), new Ghoultree(),
                    new Ghoultree(), new Ghoultree(), new Ghoultree(),
                    new Ghoultree(), new Ghoultree(), new Ghoultree()));
            harness.setHand(player1, List.of(new Ghoultree()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's graveyard creatures do not reduce the cost")
        void opponentGraveyardDoesNotReduceCost() {
            harness.setGraveyard(player2, List.of(new Ghoultree(), new Ghoultree(), new Ghoultree()));
            harness.setHand(player1, List.of(new Ghoultree()));
            // Opponent's graveyard should not help — still {7}{G} = 8 mana
            harness.addMana(player1, ManaColor.GREEN, 7);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }
}
