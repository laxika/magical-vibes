package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.d.DarkestHour;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lure;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IriniSengirTest extends BaseCardTest {

    @Nested
    @DisplayName("Green and white enchantment spells cost {2} more")
    class EnchantmentTax {

        @Test
        @DisplayName("Green enchantment can't be cast for its printed cost")
        void greenEnchantmentTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Lure()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                    findPermanent(player2, "Grizzly Bears").getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Green enchantment casts with {2} extra generic mana")
        void greenEnchantmentCastableWithTax() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Lure()));
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castEnchantment(player1, 0, findPermanent(player2, "Grizzly Bears").getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("White enchantment is taxed too")
        void whiteEnchantmentTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.setHand(player1, List.of(new CircleOfProtectionRed()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
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
            harness.setHand(player2, List.of(new CircleOfProtectionRed()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);

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
            harness.setHand(player1, List.of(new DarkestHour()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castEnchantment(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("A white creature spell is not taxed")
        void whiteCreatureNotTaxed() {
            harness.addToBattlefield(player1, new IriniSengir());
            harness.setHand(player1, List.of(new SavannahLions()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
