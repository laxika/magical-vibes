package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssemblyWorker.class, GrizzlyBears.class})
class AssemblyWorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Assembly-Worker gives a target Assembly-Worker +1/+1 until end of turn")
    void boostsTargetAssemblyWorker() {
        Permanent source = addWorker();
        Permanent target = addWorker();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addWorker();
        Permanent target = addWorker();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot target a non-Assembly-Worker creature")
    void rejectsNonAssemblyWorkerTarget() {
        Permanent source = addWorker();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = target.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    private Permanent addWorker() {
        Permanent worker = harness.addToBattlefieldAndReturn(player1, new AssemblyWorker());
        worker.setSummoningSick(false);
        return worker;
    }
}
