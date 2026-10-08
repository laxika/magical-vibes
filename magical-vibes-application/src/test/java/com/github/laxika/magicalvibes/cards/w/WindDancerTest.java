package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindDancer.class, HornedTurtle.class, Mountain.class})
class WindDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to target creature")
    void grantsFlying() {
        Permanent dancer = addCreatureReady(player1, new WindDancer());
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());
        assertThat(gqs.hasKeyword(gd, turtle, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, turtle.getId());
        assertThat(dancer.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, turtle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Tap cost prevents a second activation while Wind Dancer is tapped")
    void cannotActivateWhileTapped() {
        Permanent dancer = addCreatureReady(player1, new WindDancer());
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());

        harness.activateAbility(player1, 0, 0, null, turtle.getId());
        assertThat(dancer.isTapped()).isTrue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        addCreatureReady(player1, new WindDancer());
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());

        harness.activateAbility(player1, 0, 0, null, turtle.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, turtle, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, turtle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new WindDancer());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new WindDancer());
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());

        harness.activateAbility(player1, 0, 0, null, turtle.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, turtle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent dancer = harness.addToBattlefieldAndReturn(player1, new WindDancer());
        dancer.setSummoningSick(true);
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, turtle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is granted only to the target and only when the ability resolves")
    void grantsFlyingOnlyToTargetOnResolution() {
        addCreatureReady(player1, new WindDancer());
        Permanent target = addCreatureReady(player1, new HornedTurtle());
        Permanent other = addCreatureReady(player1, new HornedTurtle());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }
}
