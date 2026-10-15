package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTunneler.class, RuneclawBear.class, GiantGrowth.class})
class GoblinTunnelerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent target = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability makes target creature unblockable this turn")
    void resolvingAbilityMakesTargetUnblockable() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent target = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Tunneler itself is not made unblockable when targeting another creature")
    void tunnelerNotUnblockable() {
        Permanent tunneler = addCreatureReady(player1, new GoblinTunneler());
        Permanent target = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(tunneler.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent target = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target opponent's creature with power 2 or less")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent opponentCreature = addCreatureReady(player2, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Goblin Tunneler")
    void activatingAbilityTapsTunneler() {
        Permanent tunneler = addCreatureReady(player1, new GoblinTunneler());
        Permanent target = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(tunneler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent target = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItself() {
        Permanent tunneler = addCreatureReady(player1, new GoblinTunneler());

        harness.activateAbility(player1, 0, null, tunneler.getId());
        harness.passBothPriorities();

        assertThat(tunneler.isTapped()).isTrue();
        assertThat(tunneler.isCantBeBlocked()).isTrue();
    }

    @Test
    void canTargetCreatureWithExactlyTwoPower() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isCantBeBlocked()).isTrue();
    }

    @Test
    void targetGrowingBeforeResolutionMakesAbilityFail() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bear.isCantBeBlocked()).isFalse();
    }

    @Test
    void targetGrowingAfterResolutionRemainsUnblockable() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotTargetCreatureWithMoreThanTwoPower() {
        Permanent tunneler = addCreatureReady(player1, new GoblinTunneler());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tunneler.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickTunnelerCannotActivate() {
        Permanent tunneler = harness.addToBattlefieldAndReturn(player1, new GoblinTunneler());
        tunneler.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tunneler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedTunnelerCannotActivate() {
        Permanent tunneler = addCreatureReady(player1, new GoblinTunneler());
        tunneler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tunneler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    void affectedCreatureCannotBeBlockedInCombat() {
        addCreatureReady(player1, new GoblinTunneler());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void abilityStillResolvesAfterTunnelerLeavesBattlefield() {
        Permanent tunneler = addCreatureReady(player1, new GoblinTunneler());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tunneler);
        harness.passBothPriorities();

        assertThat(bear.isCantBeBlocked()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
