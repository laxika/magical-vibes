package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodshotTrainee.class, FomoriNomad.class, Imperiosaur.class, DakmorSalvage.class})
class BloodshotTraineeTest extends BaseCardTest {

    // ===== Activation with sufficient power =====

    @Test
    @DisplayName("Can activate ability when power is 4 or greater")
    void canActivateWithSufficientPower() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature when ability resolves")
    void deals4DamageToTargetCreature() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Fomori Nomad is 4/4, takes 4 damage → dies
        harness.assertInGraveyard(player2, "Fomori Nomad");
    }

    @Test
    @DisplayName("Can activate when power is exactly 4")
    void canActivateWithExactlyPower4() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fomori Nomad");
    }

    @Test
    @DisplayName("Can activate when power is greater than 4")
    void canActivateWithPowerGreaterThan4() {
        setupTraineeWithPower(6);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fomori Nomad");
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a creature that survives")
    void dealsExactlyFourDamage() {
        setupTraineeWithCreatureTarget(4, new Imperiosaur());
        Permanent target = findPermanent(player2, "Imperiosaur");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Imperiosaur");
    }

    // ===== Activation restriction =====

    @Test
    @DisplayName("Cannot activate ability with base power 2 (no boost)")
    void cannotActivateWithBasePower() {
        setupTraineeWithPower(2);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    @Test
    @DisplayName("Cannot activate ability with power 3")
    void cannotActivateWithPower3() {
        setupTraineeWithPower(3);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    // ===== Tap requirement =====

    @Test
    @DisplayName("Taps the trainee when ability is activated")
    void tapsOnActivation() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent trainee = findPermanent(player1, "Bloodshot Trainee");
        assertThat(trainee.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a noncreature permanent as the target")
    void cannotTargetNoncreaturePermanent() {
        setupTraineeWithPower(4);
        harness.addToBattlefield(player2, new DakmorSalvage());
        UUID targetId = harness.getPermanentId(player2, "Dakmor Salvage");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Still resolves if the trainee's power drops after activation")
    void resolvesAfterPowerDropsBelowActivationThreshold() {
        setupTraineeWithPower(4);
        Permanent trainee = findPermanent(player1, "Bloodshot Trainee");
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, targetId);
        trainee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fomori Nomad");
    }

    // ===== Helpers =====

    private void setupTraineeWithPower(int desiredPower) {
        setupTraineeWithCreatureTarget(desiredPower, new FomoriNomad());
    }

    private void setupTraineeWithCreatureTarget(int desiredPower, Card targetCreature) {
        Permanent trainee = addCreatureReady(player1, new BloodshotTrainee());
        addCreatureReady(player2, targetCreature);
        harness.forceActivePlayer(player1);

        // Bloodshot Trainee has base power 2; add +1/+1 counters to reach desired power
        int countersNeeded = desiredPower - 2;
        if (countersNeeded > 0) {
            trainee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, countersNeeded);
        }
    }
}
