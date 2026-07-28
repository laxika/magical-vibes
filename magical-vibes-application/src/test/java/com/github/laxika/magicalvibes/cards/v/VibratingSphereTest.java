package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VibratingSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's creatures get +2/+0 during controller's turn")
    void boostsOwnCreaturesOnControllerTurn() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller's creatures get -0/-2 during other turns")
    void shrinksOwnCreaturesOnOpponentTurn() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not affect creatures controlled by other players")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, enemyBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBears)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, enemyBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost flips as the active player changes")
    void boostFlipsWithActivePlayer() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
