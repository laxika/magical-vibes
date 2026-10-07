package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyriadConstruct;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronMachineOverlord.class, UltronDrone.class, MyriadConstruct.class, GrizzlyBears.class})
class UltronMachineOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Robots and Constructs you control get +2/+2")
    void boostsOtherRobotsAndConstructsYouControl() {
        Permanent ultron = harness.addToBattlefieldAndReturn(player1, new UltronMachineOverlord());
        Permanent robot = harness.addToBattlefieldAndReturn(player1, new UltronDrone());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new MyriadConstruct());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingRobot = harness.addToBattlefieldAndReturn(player2, new UltronDrone());

        assertThat(gqs.getEffectivePower(gd, ultron)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ultron)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonmatching)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonmatching)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingRobot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingRobot)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus starts when Ultron resolves and ends when it leaves the battlefield")
    void bonusTracksUltronsPresence() {
        Permanent robot = harness.addToBattlefieldAndReturn(player1, new UltronDrone());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new MyriadConstruct());

        harness.castFromHand(player1, new UltronMachineOverlord(), "{5}");
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(6);

        Permanent ultron = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UltronMachineOverlord)
                .findFirst().orElseThrow();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, ultron));

        harness.assertInGraveyard(player1, "Ultron, Machine Overlord");
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);
    }

    @Test
    @DisplayName("Robots entering after Ultron immediately receive the bonus")
    void boostsRobotsEnteringLater() {
        harness.addToBattlefield(player1, new UltronMachineOverlord());

        harness.castFromHand(player1, new UltronDrone(), "{3}");
        harness.passBothPriorities();

        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UltronDrone)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(5);
    }
}
