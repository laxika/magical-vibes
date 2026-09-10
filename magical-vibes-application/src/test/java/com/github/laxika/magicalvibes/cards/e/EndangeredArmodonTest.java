package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HammerheadShark;
import com.github.laxika.magicalvibes.cards.m.MorgueThrull;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndangeredArmodon.class, MorgueThrull.class, HammerheadShark.class})
class EndangeredArmodonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices when its controller controls a creature with toughness 2")
    void sacrificesWhenControllerControlsSmallCreature() {
        harness.addToBattlefield(player1, new MorgueThrull());
        harness.addToBattlefield(player1, new EndangeredArmodon());

        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Endangered Armodon");
        harness.assertInGraveyard(player1, "Endangered Armodon");
    }

    @Test
    @DisplayName("Survives while its controller controls only creatures with toughness greater than 2")
    void survivesWithoutSmallCreature() {
        harness.addToBattlefield(player1, new HammerheadShark());
        harness.addToBattlefield(player1, new EndangeredArmodon());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Endangered Armodon");
    }

    @Test
    @DisplayName("Does not trigger for a small creature controlled by an opponent")
    void doesNotTriggerForOpponentsSmallCreature() {
        harness.addToBattlefield(player1, new EndangeredArmodon());
        harness.addToBattlefield(player2, new MorgueThrull());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Endangered Armodon");
    }

    @Test
    @DisplayName("Triggers when its controller later gains a creature with toughness 2")
    void triggersWhenControllerLaterGainsSmallCreature() {
        harness.addToBattlefield(player1, new EndangeredArmodon());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new MorgueThrull());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Endangered Armodon");
        harness.assertInGraveyard(player1, "Endangered Armodon");
    }

    @Test
    @DisplayName("Sacrifices when its own toughness becomes 2")
    void sacrificesWhenItsOwnToughnessBecomesTwo() {
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        armodon.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Endangered Armodon");
        harness.assertInGraveyard(player1, "Endangered Armodon");
    }
}
