package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfKelp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Doorman.class, GrizzlyBears.class, HillGiant.class, WallOfKelp.class})
class DoormanTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature cannot be blocked by a creature with power 2 or less")
    void lowPowerCreatureCannotBlock() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        activate(doorman, target);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target creature cannot be blocked by a Wall")
    void wallCannotBlock() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new WallOfKelp());

        activate(doorman, target);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target creature can be blocked by a non-Wall creature with power 3 or greater")
    void highPowerNonWallCanBlock() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        activate(doorman, target);
        target.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction wears off at end of turn")
    void restrictionWearsOff() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        activate(doorman, target);
        target.setAttacking(true);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapping Doorman is part of the activation cost")
    void tapsAsCost() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, doorman), null, target.getId());

        assertThat(doorman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Wall with power 3 or greater still cannot block")
    void highPowerWallCannotBlock() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new WallOfKelp());
        blocker.setPowerModifier(3);

        activate(doorman, target);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature whose power increases to 3 after resolution can block")
    void powerIncreaseBeforeBlockingAllowsBlock() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        activate(doorman, target);
        blocker.setPowerModifier(1);
        target.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature whose power decreases to 2 after resolution cannot block")
    void powerDecreaseBeforeBlockingPreventsBlock() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        activate(doorman, target);
        blocker.setPowerModifier(-1);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction applies to an opponent's creature when targeted")
    void opponentsCreatureReceivesRestriction() {
        Permanent doorman = addCreatureReady(player1, new Doorman());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());

        activate(doorman, target);
        target.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, blocker), indexOf(player2, target)))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent doorman, Permanent target) {
        harness.activateAbility(player1, indexOf(player1, doorman), null, target.getId());
        harness.passBothPriorities();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
