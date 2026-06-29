package com.github.laxika.magicalvibes.cards.t;

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

class ThaliaGuardianOfThrabenTest extends BaseCardTest {

    @Nested
    @DisplayName("Noncreature spell cost increase")
    class NoncreatureSpellCostIncrease {

        @Test
        @DisplayName("Opponent's instant costs {1} more")
        void opponentInstantCostsMore() {
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            // {R} is not enough — needs {1}{R} with Thalia
            assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Opponent can cast instant with enough mana to cover the increase")
        void opponentCanCastInstantWithEnoughMana() {
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());

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
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            // {R} is not enough — Thalia's effect is symmetric
            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Controller can cast own instant with enough mana")
        void controllerCanCastOwnInstantWithEnoughMana() {
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Two Thalias stack the cost increase to {2}")
        void twoThaliasStackCostIncrease() {
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());
            harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());

            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 2);

            // {1}{R} is not enough — needs {2}{R} with two Thalias
            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Creature spells not affected")
    class CreatureSpellsNotAffected {

        @Test
        @DisplayName("Opponent's creature spell costs normal amount")
        void opponentCreatureNotAffected() {
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new GrizzlyBears()));
            harness.addMana(player2, ManaColor.GREEN, 2);

            // {1}{G} is enough — creatures are not taxed
            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Controller's own creature spell costs normal amount")
        void controllerCreatureNotAffected() {
            harness.addToBattlefield(player1, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        }
    }
}
