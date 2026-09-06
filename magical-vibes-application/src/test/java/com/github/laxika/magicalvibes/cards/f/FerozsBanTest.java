package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerozsBan.class, DancingScimitar.class, HowlingMine.class})
class FerozsBanTest extends BaseCardTest {

    @Nested
    @DisplayName("Creature spell cost increase")
    class CreatureSpellCostIncrease {

        @Test
        @DisplayName("Opponent's creature costs {2} more")
        void opponentCreatureCostsMore() {
            harness.addToBattlefield(player1, new FerozsBan());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            // {4} plus {2} = {6}; five colorless is not enough
            assertThatThrownBy(() -> harness.castFromHand(player2, new DancingScimitar(), "{5}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Opponent can cast creature with enough mana to cover the increase")
        void opponentCanCastCreatureWithEnoughMana() {
            harness.addToBattlefield(player1, new FerozsBan());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.castFromHand(player2, new DancingScimitar(), "{6}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Controller's own creature spell also costs {2} more")
        void controllerOwnCreatureCostsMore() {
            harness.addToBattlefield(player1, new FerozsBan());

            // The controller is taxed too: {6} is needed, five colorless is not enough
            assertThatThrownBy(() -> harness.castFromHand(player1, new DancingScimitar(), "{5}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Multiple copies add their cost increases")
        void multipleCopiesStack() {
            harness.addToBattlefield(player1, new FerozsBan());
            harness.addToBattlefield(player1, new FerozsBan());

            // {4} plus {2} for each Ban = {8}; all mana must be spent.
            harness.castFromHand(player1, new DancingScimitar(), "{8}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("A tapped Ban still increases creature spell costs")
        void tappedBanStillTaxesCreatureSpells() {
            var ban = harness.addToBattlefieldAndReturn(player1, new FerozsBan());
            ban.tap();

            assertThatThrownBy(() -> harness.castFromHand(player1, new DancingScimitar(), "{5}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Noncreature spells not affected")
    class NoncreatureSpellsNotAffected {

        @Test
        @DisplayName("Noncreature spell costs normal amount")
        void noncreatureSpellNotAffected() {
            harness.addToBattlefield(player1, new FerozsBan());
            // {2} is enough; noncreature spells are not taxed
            harness.castFromHand(player1, new HowlingMine(), "{2}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
