package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreenhiltTraineeTest extends BaseCardTest {

    // ===== Activation with sufficient power =====

    @Test
    @DisplayName("Can activate ability when power is 4 or greater")
    void canActivateWithSufficientPower() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Target creature gets +4/+4 until end of turn when ability resolves")
    void boostsTargetCreature() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Air Elemental is 4/4, should now be 8/8
        Permanent airElemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(airElemental.getPowerModifier()).isEqualTo(4);
        assertThat(airElemental.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate when power is greater than 4")
    void canActivateWithPowerGreaterThan4() {
        setupTraineeWithPower(6);
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent airElemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(airElemental.getPowerModifier()).isEqualTo(4);
        assertThat(airElemental.getToughnessModifier()).isEqualTo(4);
    }

    // ===== Activation restriction =====

    @Test
    @DisplayName("Cannot activate ability with base power 2 (no boost)")
    void cannotActivateWithBasePower() {
        harness.addToBattlefield(player1, new GreenhiltTrainee());
        harness.addToBattlefield(player1, new AirElemental());
        harness.forceActivePlayer(player1);

        Permanent trainee = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Greenhilt Trainee"))
                .findFirst().orElseThrow();
        trainee.setSummoningSick(false);

        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    @Test
    @DisplayName("Cannot activate ability with power 3")
    void cannotActivateWithPower3() {
        setupTraineeWithPower(3);
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    // ===== Tap requirement =====

    @Test
    @DisplayName("Taps the trainee when ability is activated")
    void tapsOnActivation() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent trainee = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Greenhilt Trainee"))
                .findFirst().orElseThrow();
        assertThat(trainee.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private void setupTraineeWithPower(int desiredPower) {
        harness.addToBattlefield(player1, new GreenhiltTrainee());
        harness.addToBattlefield(player1, new AirElemental());
        harness.forceActivePlayer(player1);

        // Greenhilt Trainee has base power 2; add +1/+1 counters to reach desired power
        Permanent trainee = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Greenhilt Trainee"))
                .findFirst().orElseThrow();
        trainee.setSummoningSick(false);
        int countersNeeded = desiredPower - 2;
        if (countersNeeded > 0) {
            trainee.setPlusOnePlusOneCounters(countersNeeded);
        }
    }
}
