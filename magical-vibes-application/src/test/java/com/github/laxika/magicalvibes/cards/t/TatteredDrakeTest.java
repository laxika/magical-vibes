package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatteredDrake.class})
class TatteredDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B} grants Tattered Drake a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent drake = addCreatureReady(player1, new TatteredDrake());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Tattered Drake cannot activate regeneration without {B}")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new TatteredDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tattered Drake can activate regeneration while tapped")
    void canActivateWhileTapped() {
        Permanent drake = addCreatureReady(player1, new TatteredDrake());
        drake.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getRegenerationShield()).isEqualTo(1);
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A regeneration shield saves Tattered Drake from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent drake = addCreatureReady(player1, new TatteredDrake());
        drake.setRegenerationShield(1);
        drake.setBlocking(true);
        drake.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new TatteredDrake());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(findPermanent(player1, "Tattered Drake")).isNotNull();
        assertThat(drake.isTapped()).isTrue();
        assertThat(drake.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Creating a regeneration shield does not tap the Drake or remove it from combat")
    void creatingShieldDoesNotRegenerateImmediately() {
        Permanent drake = addCreatureReady(player1, new TatteredDrake());
        drake.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(drake.getRegenerationShield()).isEqualTo(2);
        assertThat(drake.isTapped()).isFalse();
        assertThat(drake.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Drake can activate regeneration")
    void canActivateWithSummoningSickness() {
        Permanent drake = addCreatureReady(player1, new TatteredDrake());
        drake.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getRegenerationShield()).isEqualTo(1);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An activated regeneration shield saves the Drake and removes it from combat")
    void activatedShieldSavesFromCombat() {
        Permanent drake = addCreatureReady(player1, new TatteredDrake());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        drake.setBlocking(true);
        drake.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new TatteredDrake());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Tattered Drake");
        harness.assertInGraveyard(player2, "Tattered Drake");
        assertThat(drake.isTapped()).isTrue();
        assertThat(drake.isBlocking()).isFalse();
        assertThat(drake.getBlockingTargets()).isEmpty();
        assertThat(drake.getMarkedDamage()).isZero();
        assertThat(drake.getRegenerationShield()).isZero();
    }
}
