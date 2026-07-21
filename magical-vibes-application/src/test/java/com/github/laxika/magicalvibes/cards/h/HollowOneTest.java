package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampagingHippo;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HollowOneTest extends BaseCardTest {

    private void cycleFromHand(int times) {
        for (int i = 0; i < times; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateHandAbility(player1, 0, null);
            harness.passBothPriorities();
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Can cast for full cost {5} with no discards")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new HollowOne()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);

            harness.castArtifact(player1, 0);

            GameData gameData = harness.getGameData();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
            assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with insufficient mana when no discards")
        void cannotCastWithInsufficientMana() {
            harness.setHand(player1, List.of(new HollowOne()));
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {3} after cycling one card")
        void costReducedByOneCycle() {
            harness.setHand(player1, List.of(new RampagingHippo()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            cycleFromHand(1);

            harness.setHand(player1, List.of(new HollowOne()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.castArtifact(player1, 0);

            GameData gameData = harness.getGameData();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Costs {1} after cycling two cards")
        void costReducedByTwoCycles() {
            harness.setHand(player1, List.of(new RampagingHippo(), new RampagingHippo()));
            harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
            cycleFromHand(2);

            harness.setHand(player1, List.of(new HollowOne()));
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castArtifact(player1, 0);

            GameData gameData = harness.getGameData();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Costs {0} after cycling three or more cards")
        void costFloorsAtZero() {
            harness.setHand(player1, List.of(new RampagingHippo(), new RampagingHippo(), new RampagingHippo()));
            harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            cycleFromHand(3);

            harness.setHand(player1, List.of(new HollowOne()));
            harness.castArtifact(player1, 0);

            GameData gameData = harness.getGameData();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's discards do not reduce the cost")
        void opponentDiscardsDoNotReduceCost() {
            harness.setHand(player2, List.of(new RampagingHippo()));
            harness.setLibrary(player2, List.of(new GrizzlyBears()));
            harness.addMana(player2, ManaColor.COLORLESS, 2);
            harness.activateHandAbility(player2, 0, null);
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new HollowOne()));
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Test
    @DisplayName("Cycling discards Hollow One and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new HollowOne()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Hollow One");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
