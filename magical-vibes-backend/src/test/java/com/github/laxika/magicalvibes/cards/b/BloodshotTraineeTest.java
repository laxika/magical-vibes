package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodshotTraineeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has tap ability that deals 4 damage to target creature with power restriction")
    void hasCorrectAbility() {
        BloodshotTrainee card = new BloodshotTrainee();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.POWER_4_OR_GREATER);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);

        DealDamageToTargetCreatureEffect effect =
                (DealDamageToTargetCreatureEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.damage()).isEqualTo(4);
    }

    // ===== Activation with sufficient power =====

    @Test
    @DisplayName("Can activate ability when power is 4 or greater")
    void canActivateWithSufficientPower() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature when ability resolves")
    void deals4DamageToTargetCreature() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Air Elemental is 4/4, takes 4 damage → dies
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Can activate when power is exactly 4")
    void canActivateWithExactlyPower4() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Can activate when power is greater than 4")
    void canActivateWithPowerGreaterThan4() {
        setupTraineeWithPower(6);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    // ===== Activation restriction =====

    @Test
    @DisplayName("Cannot activate ability with base power 2 (no boost)")
    void cannotActivateWithBasePower() {
        harness.addToBattlefield(player1, new BloodshotTrainee());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);

        Permanent trainee = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bloodshot Trainee"))
                .findFirst().orElseThrow();
        trainee.setSummoningSick(false);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    @Test
    @DisplayName("Cannot activate ability with power 3")
    void cannotActivateWithPower3() {
        setupTraineeWithPower(3); // helper already clears summoning sickness
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power is 4 or greater");
    }

    // ===== Tap requirement =====

    @Test
    @DisplayName("Taps the trainee when ability is activated")
    void tapsOnActivation() {
        setupTraineeWithPower(4);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent trainee = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bloodshot Trainee"))
                .findFirst().orElseThrow();
        assertThat(trainee.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private void setupTraineeWithPower(int desiredPower) {
        harness.addToBattlefield(player1, new BloodshotTrainee());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);

        // Bloodshot Trainee has base power 2; add +1/+1 counters to reach desired power
        Permanent trainee = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bloodshot Trainee"))
                .findFirst().orElseThrow();
        trainee.setSummoningSick(false);
        int countersNeeded = desiredPower - 2;
        if (countersNeeded > 0) {
            trainee.setPlusOnePlusOneCounters(countersNeeded);
        }
    }
}
