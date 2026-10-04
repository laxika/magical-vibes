package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenProvisioner.class, GrizzlyBears.class})
class DwarvenProvisionerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control, including itself")
    void boostsCreaturesYouControl() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent provisioner = harness.addToBattlefieldAndReturn(player1, new DwarvenProvisioner());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateProvisioner(provisioner);

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, provisioner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, provisioner)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent provisioner = harness.addToBattlefieldAndReturn(player1, new DwarvenProvisioner());

        activateProvisioner(provisioner);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, provisioner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, provisioner)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations stack even while the provisioner is tapped and summoning sick")
    void repeatedActivationsStackWhileTapped() {
        Permanent provisioner = harness.addToBattlefieldAndReturn(player1, new DwarvenProvisioner());
        provisioner.setTapped(true);
        provisioner.setSummoningSick(true);

        activateProvisioner(provisioner);
        activateProvisioner(provisioner);

        assertThat(gqs.getEffectivePower(gd, provisioner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, provisioner)).isEqualTo(4);
        assertThat(provisioner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void creaturesEnteringAfterResolutionAreNotBoosted() {
        Permanent provisioner = harness.addToBattlefieldAndReturn(player1, new DwarvenProvisioner());

        activateProvisioner(provisioner);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new DwarvenProvisioner());

        assertThat(gqs.getEffectivePower(gd, provisioner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, provisioner)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the boost")
    void creaturesEnteringBeforeResolutionAreBoosted() {
        Permanent provisioner = harness.addToBattlefieldAndReturn(player1, new DwarvenProvisioner());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(provisioner), null, null);

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new DwarvenProvisioner());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, provisioner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, provisioner)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(3);
    }

    private void activateProvisioner(Permanent provisioner) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(provisioner), null, null);
        harness.passBothPriorities();
    }
}
