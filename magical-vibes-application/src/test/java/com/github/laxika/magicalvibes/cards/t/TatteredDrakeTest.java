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

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tattered Drake")).isNotNull();
        assertThat(drake.isTapped()).isTrue();
        assertThat(drake.getRegenerationShield()).isZero();
    }
}
