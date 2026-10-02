package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssemblyWorker.class, AshcoatBear.class})
class AssemblyWorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Assembly-Worker gives a target Assembly-Worker +1/+1 until end of turn")
    void boostsTargetAssemblyWorker() {
        Permanent source = addCreatureReady(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());

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
        addCreatureReady(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());

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
        Permanent source = addCreatureReady(player1, new AssemblyWorker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        UUID targetId = target.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Assembly-Worker can target itself")
    void canBoostItself() {
        Permanent worker = addCreatureReady(player1, new AssemblyWorker());

        harness.activateAbility(player1, 0, null, worker.getId());
        harness.passBothPriorities();

        assertThat(worker.isTapped()).isTrue();
        assertThat(worker.getEffectivePower()).isEqualTo(3);
        assertThat(worker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Assembly-Worker can target an opponent's tapped Assembly-Worker")
    void canBoostOpponentsTappedWorker() {
        Permanent source = addCreatureReady(player1, new AssemblyWorker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssemblyWorker());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick Assembly-Worker cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Assembly-Worker cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());
        source.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void boostResolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts from multiple Assembly-Workers are cumulative")
    void multipleBoostsAreCumulative() {
        addCreatureReady(player1, new AssemblyWorker());
        addCreatureReady(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A target that leaves and returns is not boosted by the old ability")
    void doesNotBoostReturnedTarget() {
        Permanent source = addCreatureReady(player1, new AssemblyWorker());
        Permanent target = addCreatureReady(player1, new AssemblyWorker());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, target.getCard());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(returned.getEffectivePower()).isEqualTo(2);
        assertThat(returned.getEffectiveToughness()).isEqualTo(2);
    }
}
