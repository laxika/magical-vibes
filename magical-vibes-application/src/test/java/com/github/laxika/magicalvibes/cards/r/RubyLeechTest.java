package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubyLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("Red spells you cast cost more")
    class OwnRedSpellsTaxed {

        @Test
        @DisplayName("A red spell cannot be cast without the extra mana")
        void redSpellCostsMore() {
            harness.addToBattlefield(player1, new RubyLeech());
            harness.setHand(player1, List.of(new RagingGoblin()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A red spell is castable with one extra mana")
        void redSpellCastableWithTax() {
            harness.addToBattlefield(player1, new RubyLeech());
            harness.setHand(player1, List.of(new RagingGoblin()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Only the controller's red spells are taxed")
    class OpponentAndNonRedNotTaxed {

        @Test
        @DisplayName("A non-red spell cast by the controller is not taxed")
        void nonRedSpellNotAffected() {
            harness.addToBattlefield(player1, new RubyLeech());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's red spell is not taxed")
        void opponentRedSpellNotAffected() {
            harness.addToBattlefield(player1, new RubyLeech());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new RagingGoblin()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
