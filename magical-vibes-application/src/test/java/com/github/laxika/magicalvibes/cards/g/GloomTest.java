package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GloomTest extends BaseCardTest {

    @Nested
    @DisplayName("White spells cost {3} more to cast")
    class WhiteSpellTax {

        @Test
        @DisplayName("A {W} white creature can't be cast for one white mana")
        void whiteSpellCostsThreeMore() {
            harness.addToBattlefield(player1, new Gloom());
            harness.setHand(player1, List.of(new SavannahLions()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A {W} white creature casts with {3} extra generic mana")
        void whiteSpellCastableWithTax() {
            harness.addToBattlefield(player1, new Gloom());
            harness.setHand(player1, List.of(new SavannahLions()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Non-white spells are not taxed")
        void nonWhiteSpellNotAffected() {
            harness.addToBattlefield(player1, new Gloom());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Activated abilities of white enchantments cost {3} more")
    class WhiteEnchantmentAbilityTax {

        @Test
        @DisplayName("A white enchantment's {1} ability can't be activated for one mana")
        void whiteEnchantmentAbilityCostsThreeMore() {
            harness.addToBattlefield(player1, new CircleOfProtectionRed());
            harness.addToBattlefield(player2, new Gloom());
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, (Integer) null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }

        @Test
        @DisplayName("A white enchantment's {1} ability activates with {4} mana")
        void whiteEnchantmentAbilityActivatesWithTax() {
            harness.addToBattlefield(player1, new CircleOfProtectionRed());
            harness.addToBattlefield(player2, new Gloom());
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateAbility(player1, 0, (Integer) null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Without Gloom the same ability costs only {1}")
        void whiteEnchantmentAbilityNotTaxedWithoutGloom() {
            harness.addToBattlefield(player1, new CircleOfProtectionRed());
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.activateAbility(player1, 0, (Integer) null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("A non-enchantment permanent's ability is not taxed")
        void nonEnchantmentAbilityNotAffected() {
            harness.addToBattlefield(player1, new AmaranthineWall());
            harness.addToBattlefield(player2, new Gloom());
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, (Integer) null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
