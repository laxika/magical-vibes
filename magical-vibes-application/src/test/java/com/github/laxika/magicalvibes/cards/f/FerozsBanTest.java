package com.github.laxika.magicalvibes.cards.f;

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
            harness.setHand(player2, List.of(new GrizzlyBears()));
            harness.addMana(player2, ManaColor.GREEN, 3);

            // {1}{G} plus {2} = {3}{G}; three green is not enough
            assertThatThrownBy(() -> harness.castCreature(player2, 0))
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
            harness.setHand(player2, List.of(new GrizzlyBears()));
            harness.addMana(player2, ManaColor.GREEN, 4);

            harness.castCreature(player2, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Controller's own creature spell also costs {2} more")
        void controllerOwnCreatureCostsMore() {
            harness.addToBattlefield(player1, new FerozsBan());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            // symmetric — {3}{G} needed, three green is not enough
            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Noncreature spells not affected")
    class NoncreatureSpellsNotAffected {

        @Test
        @DisplayName("Instant costs normal amount")
        void instantNotAffected() {
            harness.addToBattlefield(player1, new FerozsBan());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            // {R} is enough — noncreature spells are not taxed
            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }
}
