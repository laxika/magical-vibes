package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AysenBureaucrats;
import com.github.laxika.magicalvibes.cards.p.PrimalOrder;
import com.github.laxika.magicalvibes.cards.s.SerraAviary;
import com.github.laxika.magicalvibes.cards.t.Torture;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IriniSengir.class, PrimalOrder.class, SerraAviary.class, Torture.class,
        AysenBureaucrats.class})
class IriniSengirTest extends BaseCardTest {

    @Nested
    @DisplayName("Green and white enchantment spells cost {2} more")
    class EnchantmentTax {

        @Test
        @DisplayName("Green enchantment can't be cast for its printed cost")
        void greenEnchantmentTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());
            assertThatThrownBy(() -> harness.castFromHand(player1, new PrimalOrder(), "{2}{G}{G}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Green enchantment casts with {2} extra generic mana")
        void greenEnchantmentCastableWithTax() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.setHand(player1, List.of(new PrimalOrder()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            harness.castEnchantment(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("White enchantment is taxed too")
        void whiteEnchantmentTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());

            assertThatThrownBy(() -> harness.castFromHand(player1, new SerraAviary(), "{3}{W}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Tax is symmetric — the opponent's white enchantment is taxed as well")
        void opponentEnchantmentTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new SerraAviary()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 3);

            assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Other spells are unaffected")
    class NotTaxed {

        @Test
        @DisplayName("A black enchantment is not taxed")
        void blackEnchantmentNotTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.addToBattlefield(player2, new AysenBureaucrats());
            harness.setHand(player1, List.of(new Torture()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castEnchantment(player1, 0,
                    findPermanent(player2, "Aysen Bureaucrats").getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("A white creature spell is not taxed")
        void whiteCreatureNotTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.castFromHand(player1, new AysenBureaucrats(), "{1}{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
