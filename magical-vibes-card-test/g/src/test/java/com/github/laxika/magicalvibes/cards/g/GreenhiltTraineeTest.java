package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreenhiltTrainee.class, SpinedThopter.class})
class GreenhiltTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Can activate ability when power is 4 or greater")
    void canActivateWithSufficientPower() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Target creature gets +4/+4 until end of turn when ability resolves")
    void boostsTargetCreature() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Spined Thopter");
        assertThat(thopter.getPowerModifier()).isEqualTo(4);
        assertThat(thopter.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate when power is greater than 4")
    void canActivateWithPowerGreaterThan4() {
        setupTraineeWithPower(6);
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Spined Thopter");
        assertThat(thopter.getPowerModifier()).isEqualTo(4);
        assertThat(thopter.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate ability with base power 2 (no boost)")
    void cannotActivateWithBasePower() {
        Permanent trainee = harness.addToBattlefieldAndReturn(player1, new GreenhiltTrainee());
        harness.addToBattlefield(player1, new SpinedThopter());
        harness.forceActivePlayer(player1);
        trainee.setSummoningSick(false);

        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    @Test
    @DisplayName("Cannot activate ability with power 3")
    void cannotActivateWithPower3() {
        setupTraineeWithPower(3);
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    @Test
    @DisplayName("Taps the trainee when ability is activated")
    void tapsOnActivation() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent trainee = findPermanent(player1, "Greenhilt Trainee");
        assertThat(trainee.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost expires at cleanup")
    void boostExpiresAtCleanup() {
        setupTraineeWithPower(4);
        Permanent target = findPermanent(player1, "Spined Thopter");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        setupTraineeWithPower(4);
        Permanent trainee = findPermanent(player1, "Greenhilt Trainee");

        harness.activateAbility(player1, 0, null, trainee.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trainee)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, trainee)).isEqualTo(9);
    }

    @Test
    @DisplayName("Can boost an opponent's creature")
    void canTargetOpponentsCreature() {
        setupTraineeWithPower(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedThopter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power restriction is not rechecked when the ability resolves")
    void resolvesAfterSourcePowerDrops() {
        setupTraineeWithPower(4);
        Permanent target = findPermanent(player1, "Spined Thopter");
        harness.activateAbility(player1, 0, null, target.getId());
        findPermanent(player1, "Greenhilt Trainee").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        setupTraineeWithPower(4);
        findPermanent(player1, "Greenhilt Trainee").tap();
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick even with sufficient power")
    void cannotActivateWhileSummoningSick() {
        setupTraineeWithPower(4);
        findPermanent(player1, "Greenhilt Trainee").setSummoningSick(true);
        UUID targetId = harness.getPermanentId(player1, "Spined Thopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    private void setupTraineeWithPower(int desiredPower) {
        Permanent trainee = harness.addToBattlefieldAndReturn(player1, new GreenhiltTrainee());
        harness.addToBattlefield(player1, new SpinedThopter());
        harness.forceActivePlayer(player1);

        // Greenhilt Trainee has base power 2; add +1/+1 counters to reach desired power
        trainee.setSummoningSick(false);
        int countersNeeded = desiredPower - 2;
        if (countersNeeded > 0) {
            trainee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, countersNeeded);
        }
    }
}
