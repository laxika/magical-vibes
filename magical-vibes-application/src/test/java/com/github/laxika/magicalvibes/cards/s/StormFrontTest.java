package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormFront.class, FightingDrake.class, HornedTurtle.class, Humility.class})
class StormFrontTest extends BaseCardTest {

    @Test
    @DisplayName("Ability taps target creature with flying")
    void tapsFlyingCreature() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature with flying its controller owns")
    void tapsOwnFlyer() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player1, new FightingDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability does not tap the source, so it can be activated repeatedly")
    void canActivateRepeatedly() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer1 = addCreatureReady(player2, new FightingDrake());
        Permanent flyer2 = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, flyer1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, flyer2.getId());
        harness.passBothPriorities();

        assertThat(flyer1.isTapped()).isTrue();
        assertThat(flyer2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player2, new FightingDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        gd.playerBattlefields.get(player2.getId()).remove(flyer);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if the target loses flying before resolution")
    void fizzlesIfTargetLosesFlyingBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.addToBattlefieldAndReturn(player1, new Humility());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isFalse();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
