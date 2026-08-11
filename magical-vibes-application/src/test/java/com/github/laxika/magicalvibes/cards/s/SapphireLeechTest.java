package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerrowReejerey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SapphireLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("Blue spells you cast cost {U} more")
    class OwnBlueSpellsTaxed {

        @Test
        @DisplayName("A blue creature cannot be cast without the additional mana")
        void blueSpellCostsMore() {
            harness.addToBattlefield(player1, new SapphireLeech());
            harness.setHand(player1, List.of(new MerrowReejerey()));
            harness.addMana(player1, ManaColor.BLUE, 3);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A blue creature is castable with one extra mana")
        void blueSpellCastableWithTax() {
            harness.addToBattlefield(player1, new SapphireLeech());
            harness.setHand(player1, List.of(new MerrowReejerey()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Only the controller's blue spells are taxed")
    class OpponentAndNonBlueNotTaxed {

        @Test
        @DisplayName("A non-blue spell cast by the controller is not taxed")
        void nonBlueSpellNotAffected() {
            harness.addToBattlefield(player1, new SapphireLeech());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's blue spell is not taxed")
        void opponentBlueSpellNotAffected() {
            harness.addToBattlefield(player1, new SapphireLeech());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new MerrowReejerey()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
