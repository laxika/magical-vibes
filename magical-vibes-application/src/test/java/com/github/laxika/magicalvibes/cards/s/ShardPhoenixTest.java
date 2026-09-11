package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShardPhoenix.class, YouthfulKnight.class, SkyshroudFalcon.class})
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

            harness.assertNotOnBattlefield(player1, "Shard Phoenix");
            harness.assertInGraveyard(player1, "Shard Phoenix");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }

        @Test
        @DisplayName("Deals 2 damage to each creature without flying")
        void killsNonFlyingCreatures() {
            harness.addToBattlefield(player1, new ShardPhoenix());
            harness.addToBattlefield(player1, new YouthfulKnight()); // 2/1 no flying
            harness.addToBattlefield(player2, new YouthfulKnight());

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Youthful Knight");
            harness.assertInGraveyard(player1, "Youthful Knight");
            harness.assertNotOnBattlefield(player2, "Youthful Knight");
            harness.assertInGraveyard(player2, "Youthful Knight");
        }

        @Test
        @DisplayName("Flying creatures are not damaged")
        void doesNotDamageFlyingCreatures() {
            harness.addToBattlefield(player1, new ShardPhoenix());
            harness.addToBattlefield(player2, new SkyshroudFalcon()); // 1/1 flying

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // 1/1 flyer survives because it is not dealt damage
            harness.assertOnBattlefield(player2, "Skyshroud Falcon");
        }

        @Test
        @DisplayName("Does not damage players")
        void doesNotDamagePlayers() {
            harness.addToBattlefield(player1, new ShardPhoenix());
            harness.setLife(player2, 20);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            harness.assertLife(player2, 20);
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
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
            harness.passBothPriorities();

            harness.assertInHand(player1, "Shard Phoenix");
            harness.assertNotInGraveyard(player1, "Shard Phoenix");
        }

        @Test
        @DisplayName("Returns only the activated Shard Phoenix when multiple copies are in the graveyard")
        void returnsOnlyActivatedCopy() {
            harness.setGraveyard(player1, List.of(new ShardPhoenix(), new ShardPhoenix()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Shard Phoenix");
            harness.assertInGraveyard(player1, "Shard Phoenix");
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

        @Test
        @DisplayName("Cannot activate with three colorless mana")
        void cannotActivateWithColorlessMana() {
            harness.setGraveyard(player1, List.of(new ShardPhoenix()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
