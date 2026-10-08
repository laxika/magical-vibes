package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VibratingSphere.class, BalduvianBears.class, MarchOfTheMachines.class})
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
    @DisplayName("Also affects itself when it is a creature")
    void affectsItselfWhenItIsACreature() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new VibratingSphere());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        harness.forceActivePlayer(player1);

        assertThat(gqs.isCreature(gd, sphere)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(4);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving Sphere immediately boosts existing and subsequently entering creatures")
    void boostsCreaturesWhenItResolvesAndWhenTheyEnterLater() {
        harness.forceActivePlayer(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.castFromHand(player1, new VibratingSphere(), "{4}");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vibrating Sphere");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        Permanent laterBears = harness.enterBattlefieldAndReturn(player1, new BalduvianBears());
        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, laterBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two-toughness creatures die during another player's turn")
    void zeroToughnessCreaturesArePutIntoGraveyard() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new VibratingSphere());
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Balduvian Bears");

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player1, "Vibrating Sphere");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Multiple Spheres cumulatively modify power and toughness")
    void multipleSpheresStack() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new VibratingSphere());
        harness.addToBattlefield(player1, new VibratingSphere());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(-2);
        for (Permanent sphere : findPermanents(player1, "Vibrating Sphere")) {
            assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, sphere)).isZero();
        }

        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Vibrating Sphere");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Vibrating Sphere"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }
}
