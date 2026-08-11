package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlabasterLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("White spells you cast cost {1} more")
    class OwnWhiteSpellsTaxed {

        @Test
        @DisplayName("A {W} white creature cannot be cast for one white mana")
        void whiteSpellCostsMore() {
            harness.addToBattlefield(player1, new AlabasterLeech());
            harness.setHand(player1, List.of(new SavannahLions()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A white creature is castable with one extra generic mana")
        void whiteSpellCastableWithTax() {
            harness.addToBattlefield(player1, new AlabasterLeech());
            harness.setHand(player1, List.of(new SavannahLions()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castCreature(player1, 0);

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
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

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
            harness.setHand(player2, List.of(new SavannahLions()));
            harness.addMana(player2, ManaColor.WHITE, 1);

            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
