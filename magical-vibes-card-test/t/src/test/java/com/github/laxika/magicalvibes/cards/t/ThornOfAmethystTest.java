package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornOfAmethyst.class, LightningBolt.class, GrizzlyBears.class})
class ThornOfAmethystTest extends BaseCardTest {

    @Nested
    @DisplayName("Noncreature spell cost increase")
    @CardUsed({ThornOfAmethyst.class, LightningBolt.class})
    class NoncreatureSpellCostIncrease {

        @Test
        @DisplayName("Instant costs {1} more")
        void instantCostsMore() {
            harness.addToBattlefield(player1, new ThornOfAmethyst());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            // {R} is not enough — needs {1}{R} with Thorn of Amethyst
            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Can cast instant with enough mana to cover the increase")
        void canCastInstantWithEnoughMana() {
            harness.addToBattlefield(player1, new ThornOfAmethyst());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Effect is symmetric — opponent's noncreature spells also cost {1} more")
        void opponentInstantCostsMore() {
            harness.addToBattlefield(player1, new ThornOfAmethyst());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Creature spells not affected")
    @CardUsed({ThornOfAmethyst.class, GrizzlyBears.class})
    class CreatureSpellsNotAffected {

        @Test
        @DisplayName("Creature spell costs normal amount")
        void creatureNotAffected() {
            harness.addToBattlefield(player1, new ThornOfAmethyst());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        }
    }

    @Test
    @DisplayName("Thorn does not increase its own casting cost before entering the battlefield")
    void thornDoesNotTaxItself() {
        harness.setHand(player1, List.of(new ThornOfAmethyst()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A Thorn on the battlefield increases the cost of another Thorn")
    void anotherThornCostsMore() {
        harness.addToBattlefield(player1, new ThornOfAmethyst());
        harness.setHand(player1, List.of(new ThornOfAmethyst()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Thorns controlled by different players each add one generic mana")
    void multipleThornsStack() {
        harness.addToBattlefield(player1, new ThornOfAmethyst());
        harness.addToBattlefield(player2, new ThornOfAmethyst());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The cost increase ends when Thorn leaves the battlefield")
    void taxEndsWhenThornLeavesBattlefield() {
        harness.addToBattlefield(player1, new ThornOfAmethyst());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        var thorn = gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.setGraveyard(player1, List.of(thorn.getCard()));
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
