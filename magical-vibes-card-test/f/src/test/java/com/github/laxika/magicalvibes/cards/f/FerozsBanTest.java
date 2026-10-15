package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerozsBan.class, DancingScimitar.class, GrizzlyBears.class, HowlingMine.class})
class FerozsBanTest extends BaseCardTest {

    @Nested
    @CardUsed({FerozsBan.class, DancingScimitar.class, GrizzlyBears.class})
    @DisplayName("Creature spell cost increase")
    class CreatureSpellCostIncrease {

        @Test
        @DisplayName("Opponent's creature costs {2} more")
        void opponentCreatureCostsMore() {
            harness.addToBattlefield(player1, new FerozsBan());

            harness.forceActivePlayer(player2);
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

        @Test
        @DisplayName("Nonartifact creature pays the increase in generic mana")
        void nonartifactCreaturePaysGenericIncrease() {
            harness.addToBattlefield(player1, new FerozsBan());

            harness.castFromHand(player1, new GrizzlyBears(), "{3}{G}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("A nonartifact creature cannot be cast one generic mana short")
        void nonartifactCreatureCannotUnderpay() {
            harness.addToBattlefield(player1, new FerozsBan());

            assertThatThrownBy(() -> harness.castFromHand(player1, new GrizzlyBears(), "{2}{G}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Copies controlled by different players add their increases")
        void copiesWithDifferentControllersStack() {
            harness.addToBattlefield(player1, new FerozsBan());
            harness.addToBattlefield(player2, new FerozsBan());

            harness.castFromHand(player1, new DancingScimitar(), "{8}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("A Ban in the graveyard does not increase costs")
        void banInGraveyardDoesNotTax() {
            harness.setGraveyard(player1, List.of(new FerozsBan()));

            harness.castFromHand(player1, new DancingScimitar(), "{4}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }
    }

    @Nested
    @CardUsed({FerozsBan.class, HowlingMine.class})
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
