package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShardPhoenixTest extends BaseCardTest {

    // ===== Sacrifice ability =====

    @Nested
    @DisplayName("Sacrifice ability")
    class SacrificeAbilityTests {

        @Test
        @DisplayName("Activating sacrifices Shard Phoenix and puts ability on the stack")
        void activatingSacrificesAndPutsOnStack() {
            harness.addToBattlefield(player1, new ShardPhoenix());

            harness.activateAbility(player1, 0, null, null);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Shard Phoenix"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Shard Phoenix"));

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }

        @Test
        @DisplayName("Deals 2 damage to each creature without flying")
        void killsNonFlyingCreatures() {
            harness.addToBattlefield(player1, new ShardPhoenix());
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 no flying

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Flying creatures are not damaged")
        void doesNotDamageFlyingCreatures() {
            harness.addToBattlefield(player1, new ShardPhoenix());
            harness.addToBattlefield(player2, new SuntailHawk()); // 1/1 flying

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // 1/1 flyer survives because it is not dealt damage
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        }

        @Test
        @DisplayName("Does not damage players")
        void doesNotDamagePlayers() {
            harness.addToBattlefield(player1, new ShardPhoenix());
            harness.setLife(player2, 20);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }
    }

    // ===== Graveyard activated ability =====

    @Nested
    @DisplayName("Graveyard activated ability")
    class GraveyardAbilityTests {

        @Test
        @DisplayName("Returns Shard Phoenix to hand when activated during your upkeep")
        void returnsToHandDuringUpkeep() {
            harness.setGraveyard(player1, List.of(new ShardPhoenix()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Shard Phoenix"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Shard Phoenix"));
        }

        @Test
        @DisplayName("Cannot activate outside your upkeep")
        void cannotActivateOutsideUpkeep() {
            harness.setGraveyard(player1, List.of(new ShardPhoenix()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("upkeep");
        }

        @Test
        @DisplayName("Cannot activate on an opponent's upkeep")
        void cannotActivateOnOpponentUpkeep() {
            harness.setGraveyard(player1, List.of(new ShardPhoenix()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.UPKEEP);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("upkeep");
        }

        @Test
        @DisplayName("Cannot activate without {R}{R}{R}")
        void cannotActivateWithoutMana() {
            harness.setGraveyard(player1, List.of(new ShardPhoenix()));
            harness.addMana(player1, ManaColor.RED, 2); // not enough
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
