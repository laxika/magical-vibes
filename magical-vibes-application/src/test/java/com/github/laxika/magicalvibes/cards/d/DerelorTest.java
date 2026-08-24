package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodPet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Derelor.class, BloodPet.class, GrizzlyBears.class})
class DerelorTest extends BaseCardTest {

    @Nested
    @DisplayName("Black spells you cast cost more")
    class OwnBlackSpellsTaxed {

        @Test
        @DisplayName("Controller's black spell costs {B} more (single black not enough)")
        void blackSpellCostsMore() {
            harness.addToBattlefield(player1, new Derelor());

            // Blood Pet is {B}; Derelor adds another black mana to the total cost.
            assertThatThrownBy(() -> harness.castFromHand(player1, new BloodPet(), "{B}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Controller's black spell castable with enough mana to cover the tax")
        void blackSpellCastableWithTax() {
            harness.addToBattlefield(player1, new Derelor());
            harness.castFromHand(player1, new BloodPet(), "{B}{B}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Colored tax cannot be paid with colorless mana")
        void blackSpellCannotUseColorlessManaForTax() {
            harness.addToBattlefield(player1, new Derelor());
            harness.setHand(player1, List.of(new BloodPet()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
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
            harness.castFromHand(player2, new BloodPet(), "{B}");

            // Derelor only taxes its controller's spells, so a single {B} pays Blood Pet in full
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Controller's non-black spell is not taxed")
        void nonBlackSpellNotAffected() {
            harness.addToBattlefield(player1, new Derelor());
            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

            // {1}{G} is enough — non-black spells are not taxed
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
