package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CuboidColony.class, GrizzlyBears.class, Shock.class})
class CuboidColonyTest extends BaseCardTest {

    private Permanent addColony(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CuboidColony());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Nested
    @CardUsed({CuboidColony.class})
    @DisplayName("Flash")
    class FlashTests {

        @Test
        @DisplayName("Can cast during opponent's turn")
        void canCastDuringOpponentsTurn() {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player1, List.of(new CuboidColony()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.getGameService().passPriority(harness.getGameData(), player2);
            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        }

        @Test
        @DisplayName("Can cast during combat step")
        void canCastDuringCombat() {
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();

            harness.setHand(player1, List.of(new CuboidColony()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        }
    }

    @Nested
    @CardUsed({CuboidColony.class, GrizzlyBears.class, Shock.class})
    @DisplayName("Increment")
    class IncrementTests {

        @Test
        @DisplayName("Casting a two-mana spell puts a +1/+1 counter on the 1/1 (2 > 1)")
        void twoManaSpellAddsCounter() {
            Permanent colony = addColony(player1);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities();

            assertThat(colony.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Casting a one-mana spell adds no counter (1 is not greater than power or toughness)")
        void oneManaSpellAddsNoCounter() {
            Permanent colony = addColony(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new Shock()));
            harness.castAndResolveInstant(player1, 0, player2.getId());

            assertThat(colony.getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        void opponentsSpellDoesNotIncrement() {
            Permanent colony = addColony(player1);
            setUpMainPhase(player2);

            harness.castFromHand(player2, new CuboidColony(), "{G}{U}");
            resolveAllTriggers();

            assertThat(colony.getPlusOnePlusOneCounters()).isZero();
            assertThat(findPermanent(player2, "Cuboid Colony").getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        void colonyDoesNotIncrementFromItsOwnCast() {
            setUpMainPhase(player1);

            harness.castFromHand(player1, new CuboidColony(), "{G}{U}");
            resolveAllTriggers();

            assertThat(findPermanent(player1, "Cuboid Colony").getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        void equalManaSpentDoesNotIncrementAnAlreadyGrownColony() {
            Permanent colony = addColony(player1);
            colony.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new CuboidColony(), "{G}{U}");
            resolveAllTriggers();

            assertThat(colony.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        void overlappingTriggersRecheckSizeAtResolution() {
            Permanent colony = addColony(player1);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new CuboidColony(), "{G}{U}");
            harness.castFromHand(player1, new CuboidColony(), "{G}{U}");
            resolveAllTriggers();

            assertThat(colony.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(countPermanents(player1, "Cuboid Colony")).isEqualTo(3);
        }

        @Test
        void manaGreaterThanOnlyToughnessStillIncrements() {
            Permanent colony = addColony(player1);
            colony.setPowerModifier(2);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new CuboidColony(), "{G}{U}");
            resolveAllTriggers();

            assertThat(colony.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        void manaGreaterThanOnlyPowerStillIncrements() {
            Permanent colony = addColony(player1);
            colony.setToughnessModifier(2);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new CuboidColony(), "{G}{U}");
            resolveAllTriggers();

            assertThat(colony.getPlusOnePlusOneCounters()).isEqualTo(1);
        }
    }

    @Test
    void groundCreatureCannotBlockColony() {
        addColony(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void grownColonyTramplesOverFlyingBlocker() {
        Permanent attacker = addColony(player1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addColony(player2);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Cuboid Colony");
        harness.assertOnBattlefield(player1, "Cuboid Colony");
    }
}
