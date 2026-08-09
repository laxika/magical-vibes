package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EndangeredArmodonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices when its controller controls a creature with toughness 2")
    void sacrificesWhenControllerControlsSmallCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new EndangeredArmodon());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endangered Armodon");
        harness.assertInGraveyard(player1, "Endangered Armodon");
    }

    @Test
    @DisplayName("Survives while its controller controls only creatures with toughness greater than 2")
    void survivesWithoutSmallCreature() {
        harness.addToBattlefield(player1, new CentaurCourser());
        harness.addToBattlefield(player1, new EndangeredArmodon());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Endangered Armodon");
    }

    @Test
    @DisplayName("Sacrifices when its own toughness becomes 2")
    void sacrificesWhenItsOwnToughnessBecomesTwo() {
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        armodon.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endangered Armodon");
        harness.assertInGraveyard(player1, "Endangered Armodon");
    }
}
