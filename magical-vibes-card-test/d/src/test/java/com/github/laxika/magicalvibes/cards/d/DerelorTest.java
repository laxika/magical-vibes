package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodPet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Derelor.class, BloodPet.class, GrizzlyBears.class, Humility.class})
class DerelorTest extends BaseCardTest {

    @Nested
    @DisplayName("Black spells you cast cost more")
    @CardUsed({Derelor.class, BloodPet.class})
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
        @DisplayName("Each Derelor adds its own colored tax")
        void multipleDerelorsStackTheirTaxes() {
            harness.addToBattlefield(player1, new Derelor());
            harness.addToBattlefield(player1, new Derelor());
            harness.castFromHand(player1, new BloodPet(), "{B}{B}{B}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Colored tax cannot be paid with colorless mana")
        void blackSpellCannotUseColorlessManaForTax() {
            harness.addToBattlefield(player1, new Derelor());
            assertThatThrownBy(() -> harness.castFromHand(player1, new BloodPet(), "{B}{1}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Only the controller's own spells are taxed")
    @CardUsed({Derelor.class, BloodPet.class, GrizzlyBears.class})
    class OpponentAndNonBlackNotTaxed {

        @Test
        @DisplayName("Opponent's black spell is not taxed")
        void opponentBlackSpellNotTaxed() {
            harness.addToBattlefield(player1, new Derelor());

            harness.forceActivePlayer(player2);
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

    @Test
    @DisplayName("Derelor does not tax itself before entering the battlefield")
    void firstDerelorDoesNotTaxItself() {
        harness.castFromHand(player1, new Derelor(), "{3}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Derelor");
    }

    @Test
    @DisplayName("A battlefield Derelor taxes another Derelor being cast")
    void secondDerelorPaysOnlyExistingDerelorTax() {
        harness.addToBattlefield(player1, new Derelor());
        harness.castFromHand(player1, new Derelor(), "{3}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({Derelor.class, BloodPet.class, Humility.class})
    @DisplayName("Derelor's tax stops when Humility removes its abilities")
    void losingAbilitiesRemovesTax() {
        harness.addToBattlefield(player1, new Derelor());
        harness.castFromHand(player1, new Humility(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Humility");

        harness.castFromHand(player1, new BloodPet(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
