package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WitherbloomCampus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallMonitor.class, WitherbloomCampus.class})
class HallMonitorTest extends BaseCardTest {

    @Test
    void activatedAbilityPreventsTargetFromBlocking() {
        addCreatureReady(player1, new HallMonitor());
        addCreatureReady(player1, new HallMonitor());
        Permanent blocker = addCreatureReady(player2, new HallMonitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HallMonitor());
        Permanent target = addCreatureReady(player2, new HallMonitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new HallMonitor());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WitherbloomCampus());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void tappingHallMonitorPreventsImmediateReactivation() {
        addCreatureReady(player1, new HallMonitor());
        Permanent target = addCreatureReady(player2, new HallMonitor());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hasteAllowsTapAbilityOnTurnItEnters() {
        Permanent monitor = harness.addToBattlefieldAndReturn(player1, new HallMonitor());
        Permanent target = addCreatureReady(player2, new HallMonitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(monitor.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void hasteAllowsAttackingOnTurnItEnters() {
        Permanent monitor = harness.addToBattlefieldAndReturn(player1, new HallMonitor());
        addCreatureReady(player2, new HallMonitor());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(monitor.isAttacking()).isTrue();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent monitor = addCreatureReady(player1, new HallMonitor());
        Permanent target = addCreatureReady(player2, new HallMonitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(monitor);
        gd.playerGraveyards.get(player1.getId()).add(monitor.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent monitor = addCreatureReady(player1, new HallMonitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, monitor.getId());
        harness.passBothPriorities();

        assertThat(monitor.isCantBlockThisTurn()).isTrue();
    }
}
