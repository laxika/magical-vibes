package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonWall.class})
class DemonWallTest extends BaseCardTest {

    @Test
    @DisplayName("Demon Wall cannot attack while it has no counters")
    void cannotAttackWithoutCounters() {
        Permanent wall = addReadyDemonWall();
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Demon Wall can attack with any kind of counter")
    void canAttackWithAnyCounter() {
        Permanent wall = addReadyDemonWall();
        wall.setCounterCount(CounterType.CHARGE, 1);
        beginDeclareAttackers();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(indexOf(wall))));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Activating Demon Wall's ability puts two +1/+1 counters on it")
    void activationPutsTwoCountersAndAllowsAttacking() {
        Permanent wall = addReadyDemonWall();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, indexOf(wall), null, null);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        beginDeclareAttackers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(indexOf(wall))));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    void cannotAttackAfterLastCounterIsRemoved() {
        Permanent wall = addReadyDemonWall();
        wall.setCounterCount(CounterType.CHARGE, 1);
        wall.setCounterCount(CounterType.CHARGE, 0);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void counterDoesNotOverrideSummoningSickness() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new DemonWall());
        wall.setSummoningSick(true);
        wall.setCounterCount(CounterType.CHARGE, 1);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new DemonWall());
        wall.setSummoningSick(true);
        wall.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, indexOf(wall), null, null);
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    void menaceRejectsOneBlocker() {
        Permanent wall = addReadyDemonWall();
        wall.setCounterCount(CounterType.CHARGE, 1);
        harness.addToBattlefield(player2, new DemonWall());
        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(wall)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        Permanent wall = addReadyDemonWall();
        wall.setCounterCount(CounterType.CHARGE, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DemonWall());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DemonWall());
        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(wall)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private Permanent addReadyDemonWall() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new DemonWall());
        wall.setSummoningSick(false);
        return wall;
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
