package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighGround;
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

@CardUsed({JukaiTrainee.class, GrizzlyBears.class, HighGround.class})
class JukaiTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Jukai Trainee +1/+1 until end of turn")
    void blockingGivesBoost() {
        Permanent trainee = addReadyTrainee(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(trainee.getPowerModifier()).isEqualTo(1);
        assertThat(trainee.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked gives Jukai Trainee +1/+1 until end of turn")
    void becomingBlockedGivesBoost() {
        Permanent trainee = addReadyTrainee(player1);
        trainee.setAttacking(true);
        addReadyAttacker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(trainee.getPowerModifier()).isEqualTo(1);
        assertThat(trainee.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked triggers only once with multiple blockers")
    void multipleBlockersGiveOnlyOneBoost() {
        Permanent trainee = addReadyTrainee(player1);
        trainee.setAttacking(true);
        addReadyAttacker(player2);
        addReadyAttacker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(trainee.getPowerModifier()).isEqualTo(1);
        assertThat(trainee.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent trainee = addReadyTrainee(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(trainee.getPowerModifier()).isZero();
        assertThat(trainee.getToughnessModifier()).isZero();
    }

    private Permanent addReadyTrainee(Player player) {
        return addCreatureReady(player, new JukaiTrainee());
    }

    private void addReadyAttacker(Player player) {
        Permanent permanent = addCreatureReady(player, new GrizzlyBears());
        permanent.setAttacking(true);
    }

    @Test
    @DisplayName("Blocking multiple attackers gives only one +1/+1 boost")
    void blockingMultipleAttackersGivesOnlyOneBoost() {
        Permanent trainee = addReadyTrainee(player2);
        harness.addToBattlefield(player2, new HighGround());
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));
        resolveAllTriggers();

        assertThat(trainee.getPowerModifier()).isEqualTo(1);
        assertThat(trainee.getToughnessModifier()).isEqualTo(1);
    }
}
