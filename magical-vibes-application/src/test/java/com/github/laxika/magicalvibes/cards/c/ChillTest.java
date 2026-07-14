package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChillTest extends BaseCardTest {

    @Nested
    @DisplayName("Red spell cost increase")
    class RedSpellCostIncrease {

        @Test
        @DisplayName("Red spell costs {2} more")
        void redSpellCostsMore() {
            harness.addToBattlefield(player1, new Chill());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            // {R} plus {2} = {2}{R}; a single red is not enough
            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Red spell castable with enough mana to cover the increase")
        void redSpellCastableWithEnoughMana() {
            harness.addToBattlefield(player1, new Chill());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's red spell also costs {2} more")
        void opponentRedSpellCostsMore() {
            harness.addToBattlefield(player1, new Chill());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Non-red spells not affected")
    class NonRedSpellsNotAffected {

        @Test
        @DisplayName("Green creature costs normal amount")
        void greenCreatureNotAffected() {
            harness.addToBattlefield(player1, new Chill());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            // {1}{G} is enough — non-red spells are not taxed
            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
