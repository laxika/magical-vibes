package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class VanquishTheHordeTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Cannot cast with insufficient mana and no creatures")
        void cannotCastWithInsufficientMana() {
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Can cast for full cost {6}{W}{W} with no creatures")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 8);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            assertThat(entry.getCard().getName()).isEqualTo("Vanquish the Horde");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost is reduced by 1 for each creature on the battlefield")
        void costReducedByCreatureCount() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            // 2 creatures = cost reduced by 2, so {4}{W}{W} = 6 mana
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost cannot be reduced below {W}{W}")
        void costCannotGoBelowColoredMana() {
            for (int i = 0; i < 4; i++) {
                harness.addToBattlefield(player1, new GrizzlyBears());
            }
            for (int i = 0; i < 3; i++) {
                harness.addToBattlefield(player2, new GrizzlyBears());
            }
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            // 7 creatures → generic floors at 0; still need {W}{W}
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Insufficient mana with partial cost reduction still fails")
        void insufficientManaWithPartialReduction() {
            harness.addToBattlefield(player1, new GrizzlyBears()); // cost {5}{W}{W} = 7
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Destroy all creatures")
    class DestroyEffect {

        @Test
        @DisplayName("Destroys all creatures on both battlefields")
        void destroysAllCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 6); // 2 creatures → {4}{W}{W}

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Vanquish the Horde"));
        }
    }
}
