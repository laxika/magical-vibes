package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HourOfRevelation.class, GrizzlyBears.class, Plains.class, Manalith.class, Solemnity.class})
class HourOfRevelationTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({HourOfRevelation.class, GrizzlyBears.class, Plains.class, Manalith.class, Solemnity.class})
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

        @Test
        void nineNonlandPermanentsDoNotReduceCost() {
            for (int i = 0; i < 9; i++) {
                harness.addToBattlefield(player2, new Manalith());
            }
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void opponentsArtifactsAndEnchantmentsCountForReduction() {
            for (int i = 0; i < 9; i++) {
                harness.addToBattlefield(player2, new Manalith());
            }
            harness.addToBattlefield(player2, new Solemnity());
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 3);

            harness.castSorcery(player1, 0, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        void reductionDoesNotRemoveWhiteManaRequirements() {
            for (int i = 0; i < 10; i++) {
                harness.addToBattlefield(player2, new Manalith());
            }
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Destroy all nonland permanents")
    @CardUsed({HourOfRevelation.class, GrizzlyBears.class, Plains.class, Manalith.class, Solemnity.class})
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

            harness.castAndResolveSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Plains");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Plains");
            harness.assertInGraveyard(player1, "Hour of Revelation");
        }

        @Test
        void destroysArtifactsAndEnchantmentsForBothPlayers() {
            harness.addToBattlefield(player1, new Manalith());
            harness.addToBattlefield(player1, new Solemnity());
            harness.addToBattlefield(player2, new Manalith());
            harness.addToBattlefield(player2, new Solemnity());
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castAndResolveSorcery(player1, 0, 0);

            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Manalith");
            harness.assertInGraveyard(player1, "Solemnity");
            harness.assertInGraveyard(player2, "Manalith");
            harness.assertInGraveyard(player2, "Solemnity");
        }

        @Test
        void resolvesWithNoPermanents() {
            harness.setHand(player1, List.of(new HourOfRevelation()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castAndResolveSorcery(player1, 0, 0);

            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Hour of Revelation");
        }
    }
}
