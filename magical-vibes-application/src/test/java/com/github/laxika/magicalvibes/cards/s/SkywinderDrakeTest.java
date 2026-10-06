package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkywinderDrake.class, StormfrontPegasus.class, RuneclawBear.class, GiantSpider.class, Levitation.class})
class SkywinderDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Skywinder Drake can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new SkywinderDrake());
        drake.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new StormfrontPegasus());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Skywinder Drake cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new SkywinderDrake());
        drake.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Skywinder Drake's flying lets it attack past a ground blocker")
    void flyingPreventsGroundBlock() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SkywinderDrake());
        drake.setSummoningSick(false);
        drake.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reach does not let an attacker satisfy Skywinder Drake's flying restriction")
    void cannotBlockAttackerWithReachOnly() {
        harness.addToBattlefield(player2, new SkywinderDrake());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("A creature granted flying can be blocked by Skywinder Drake")
    void canBlockCreatureWithGrantedFlying() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new SkywinderDrake());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Levitation());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block an attacking Skywinder Drake")
    void reachCreatureCanBlockAttackingDrake() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SkywinderDrake());
        drake.setSummoningSick(false);
        drake.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
