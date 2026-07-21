package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HourOfRevelationTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs full {3}{W}{W}{W} with fewer than ten nonland permanents")
        void fullCostBelowThreshold() {
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Costs {3} less ({W}{W}{W}) with ten or more nonland permanents")
        void reducedAtThreshold() {
            for (int i = 0; i < 5; i++) {
                harness.addToBattlefield(player1, new GrizzlyBears());
                harness.addToBattlefield(player2, new GrizzlyBears());
            }
            harness.setHand(player1, List.of(new HourOfRevelation()));
            // Ten nonland permanents => costs {W}{W}{W} = 3 mana.
            harness.addMana(player1, ManaColor.WHITE, 3);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Lands do not count toward the threshold")
        void landsDoNotCount() {
            for (int i = 0; i < 5; i++) {
                harness.addToBattlefield(player1, new GrizzlyBears());
                harness.addToBattlefield(player2, new Plains());
            }
            harness.setHand(player1, List.of(new HourOfRevelation()));
            // Only five nonland permanents => no reduction; {W}{W}{W} alone is insufficient.
            harness.addMana(player1, ManaColor.WHITE, 3);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Destroy all nonland permanents")
    class DestroyEffect {

        @Test
        @DisplayName("Destroys every nonland permanent but spares lands")
        void destroysNonlandSparesLands() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new Plains());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new Plains());
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .anyMatch(p -> p.getCard().getName().equals("Plains"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .anyMatch(p -> p.getCard().getName().equals("Plains"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Hour of Revelation"));
        }
    }
}
