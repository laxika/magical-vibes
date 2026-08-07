package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VrynWingmareTest extends BaseCardTest {

    @Nested
    @DisplayName("Noncreature spell cost increase")
    class NoncreatureSpellCostIncrease {

        @Test
        @DisplayName("Opponent's instant costs {1} more")
        void opponentInstantCostsMore() {
            harness.addToBattlefield(player1, new VrynWingmare());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Opponent can cast the instant with enough mana to cover the increase")
        void opponentCanCastInstantWithEnoughMana() {
            harness.addToBattlefield(player1, new VrynWingmare());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 2);

            harness.castInstant(player2, 0, player1.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Controller's own noncreature spells also cost {1} more")
        void controllerOwnInstantCostsMore() {
            harness.addToBattlefield(player1, new VrynWingmare());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Two Wingmares stack the cost increase to {2}")
        void twoWingmaresStackCostIncrease() {
            harness.addToBattlefield(player1, new VrynWingmare());
            harness.addToBattlefield(player2, new VrynWingmare());

            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Creature spells not affected")
    class CreatureSpellsNotAffected {

        @Test
        @DisplayName("Opponent's creature spell costs the normal amount")
        void opponentCreatureNotAffected() {
            harness.addToBattlefield(player1, new VrynWingmare());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new GrizzlyBears()));
            harness.addMana(player2, ManaColor.GREEN, 2);

            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        }
    }
}
