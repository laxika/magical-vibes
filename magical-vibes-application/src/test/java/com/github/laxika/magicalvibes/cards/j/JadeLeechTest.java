package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JadeLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("Green spells you cast cost {G} more")
    class OwnGreenSpellsTaxed {

        @Test
        @DisplayName("A green spell cannot be cast without the extra mana")
        void greenSpellCostsMore() {
            harness.addToBattlefield(player1, new JadeLeech());
            harness.setHand(player1, List.of(new LlanowarElves()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A green spell is castable with one extra mana")
        void greenSpellCastableWithTax() {
            harness.addToBattlefield(player1, new JadeLeech());
            harness.setHand(player1, List.of(new LlanowarElves()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Only the controller's green spells are taxed")
    class OpponentAndNonGreenNotTaxed {

        @Test
        @DisplayName("A non-green spell cast by the controller is not taxed")
        void nonGreenSpellNotAffected() {
            harness.addToBattlefield(player1, new JadeLeech());
            harness.setHand(player1, List.of(new RagingGoblin()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's green spell is not taxed")
        void opponentGreenSpellNotAffected() {
            harness.addToBattlefield(player1, new JadeLeech());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LlanowarElves()));
            harness.addMana(player2, ManaColor.GREEN, 1);

            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
