package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HornedCheetah;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabasterLeech.class, ArdentSoldier.class, HolyDay.class, HornedCheetah.class,
        MetathranZombie.class})
class AlabasterLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("White spells you cast cost {W} more")
    class OwnWhiteSpellsTaxed {

        @Test
        @DisplayName("A white creature cannot be cast without the additional {W}")
        void whiteSpellCostsMore() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new ArdentSoldier(), "{1}{W}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A white creature is castable with one extra white mana")
        void whiteSpellCastableWithTax() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            harness.castFromHand(player1, new ArdentSoldier(), "{1}{W}{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Colorless mana cannot pay the additional {W}")
        void whiteSpellTaxRequiresWhiteMana() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new ArdentSoldier(), "{2}{W}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A multicolored spell containing white is taxed")
        void multicoloredWhiteSpellIsTaxed() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            harness.castFromHand(player1, new HornedCheetah(), "{2}{G}{W}{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("A white noncreature spell is taxed")
        void whiteNoncreatureSpellIsTaxed() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            harness.castFromHand(player1, new HolyDay(), "{W}{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Only the controller's white spells are taxed")
    class OpponentAndNonWhiteNotTaxed {

        @Test
        @DisplayName("A non-white spell cast by the controller is not taxed")
        void nonWhiteSpellNotAffected() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            harness.castFromHand(player1, new MetathranZombie(), "{1}{U}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's white spell is not taxed")
        void opponentWhiteSpellNotAffected() {
            harness.addToBattlefield(player1, new AlabasterLeech());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();

            harness.castFromHand(player2, new ArdentSoldier(), "{1}{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
