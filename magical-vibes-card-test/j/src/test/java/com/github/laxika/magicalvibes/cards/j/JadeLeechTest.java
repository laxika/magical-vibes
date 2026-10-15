package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HornedCheetah;
import com.github.laxika.magicalvibes.cards.s.SunscapeApprentice;
import com.github.laxika.magicalvibes.cards.t.ThornscapeApprentice;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JadeLeech.class, ThornscapeApprentice.class, SunscapeApprentice.class, HornedCheetah.class})
class JadeLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("Green spells you cast cost {G} more")
    @CardUsed({JadeLeech.class, ThornscapeApprentice.class, HornedCheetah.class})
    class OwnGreenSpellsTaxed {

        @Test
        @DisplayName("A green spell cannot be cast without the extra mana")
        void greenSpellCostsMore() {
            harness.addToBattlefield(player1, new JadeLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new ThornscapeApprentice(), "{G}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A green spell is castable with one extra mana")
        void greenSpellCastableWithTax() {
            harness.addToBattlefield(player1, new JadeLeech());

            harness.castFromHand(player1, new ThornscapeApprentice(), "{G}{G}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("A multicolored green spell is taxed")
        void multicoloredGreenSpellIsTaxed() {
            harness.addToBattlefield(player1, new JadeLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new HornedCheetah(), "{2}{G}{W}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("The additional green mana cannot be paid with colorless mana")
        void greenSpellTaxRequiresGreenMana() {
            harness.addToBattlefield(player1, new JadeLeech());
            assertThatThrownBy(() -> harness.castFromHand(player1, new ThornscapeApprentice(), "{1}{G}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void multipleLeechesEachAddGreenMana() {
            harness.addToBattlefield(player1, new JadeLeech());
            harness.addToBattlefield(player1, new JadeLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new ThornscapeApprentice(), "{G}{G}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void multipleLeechTaxesCanBePaid() {
            harness.addToBattlefield(player1, new JadeLeech());
            harness.addToBattlefield(player1, new JadeLeech());

            harness.castFromHand(player1, new ThornscapeApprentice(), "{G}{G}{G}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        void jadeLeechDoesNotTaxItselfWhileBeingCast() {
            harness.castFromHand(player1, new JadeLeech(), "{2}{G}{G}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        void existingLeechTaxesAnotherLeech() {
            harness.addToBattlefield(player1, new JadeLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new JadeLeech(), "{2}{G}{G}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Only the controller's green spells are taxed")
    @CardUsed({JadeLeech.class, SunscapeApprentice.class, ThornscapeApprentice.class})
    class OpponentAndNonGreenNotTaxed {

        @Test
        @DisplayName("A non-green spell cast by the controller is not taxed")
        void nonGreenSpellNotAffected() {
            harness.addToBattlefield(player1, new JadeLeech());

            harness.castFromHand(player1, new SunscapeApprentice(), "{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's green spell is not taxed")
        void opponentGreenSpellNotAffected() {
            harness.addToBattlefield(player1, new JadeLeech());
            harness.forceActivePlayer(player2);

            harness.castFromHand(player2, new ThornscapeApprentice(), "{G}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
