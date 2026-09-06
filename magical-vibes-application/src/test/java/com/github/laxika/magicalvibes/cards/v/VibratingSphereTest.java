package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VibratingSphere.class, BalduvianBears.class})
class VibratingSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's creatures get +2/+0 during controller's turn")
    void boostsOwnCreaturesOnControllerTurn() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller's creatures get -0/-2 during other turns")
    void shrinksOwnCreaturesOnOpponentTurn() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not affect creatures controlled by other players")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new VibratingSphere());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @CardUsed(MarchOfTheMachines.class)
    @DisplayName("Also affects itself when it is a creature")
    void affectsItselfWhenItIsACreature() {
        harness.addToBattlefield(player1, new VibratingSphere());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent sphere = findPermanent(player1, "Vibrating Sphere");

        harness.forceActivePlayer(player1);

        assertThat(gqs.isCreature(gd, sphere)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(4);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(2);
    }
}
