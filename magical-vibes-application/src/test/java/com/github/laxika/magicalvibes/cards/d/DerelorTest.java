package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodPet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerelorTest extends BaseCardTest {

    @Nested
    @DisplayName("Black spells you cast cost more")
    class OwnBlackSpellsTaxed {

        @Test
        @DisplayName("Controller's black spell costs {1} more (single black not enough)")
        void blackSpellCostsMore() {
            harness.addToBattlefield(player1, new Derelor());
            harness.setHand(player1, List.of(new BloodPet()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            // BloodPet is {B}; with Derelor's tax it costs {B} plus {1} = two mana
            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Controller's black spell castable with enough mana to cover the tax")
        void blackSpellCastableWithTax() {
            harness.addToBattlefield(player1, new Derelor());
            harness.setHand(player1, List.of(new BloodPet()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Only the controller's own spells are taxed")
    class OpponentAndNonBlackNotTaxed {

        @Test
        @DisplayName("Opponent's black spell is not taxed")
        void opponentBlackSpellNotTaxed() {
            harness.addToBattlefield(player1, new Derelor());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new BloodPet()));
            harness.addMana(player2, ManaColor.BLACK, 1);

            // Derelor only taxes its controller's spells, so a single {B} pays BloodPet in full
            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Controller's non-black spell is not taxed")
        void nonBlackSpellNotAffected() {
            harness.addToBattlefield(player1, new Derelor());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);

            // {1}{G} is enough — non-black spells are not taxed
            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
