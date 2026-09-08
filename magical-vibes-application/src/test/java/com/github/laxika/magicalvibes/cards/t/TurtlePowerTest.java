package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtlePower.class, HornedTurtle.class, GrizzlyBears.class})
class TurtlePowerTest extends BaseCardTest {

    @Test
    @DisplayName("Turtles you control get +2/+2")
    void buffsTurtlesYouControl() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        int basePower = gqs.getEffectivePower(gd, turtle);
        int baseToughness = gqs.getEffectiveToughness(gd, turtle);

        harness.addToBattlefield(player1, new TurtlePower());

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Does not buff non-Turtle creatures")
    void doesNotBuffNonTurtles() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        harness.addToBattlefield(player1, new TurtlePower());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff an opponent's Turtles")
    void doesNotBuffOpponentsTurtles() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        int basePower = gqs.getEffectivePower(gd, turtle);
        int baseToughness = gqs.getEffectiveToughness(gd, turtle);

        harness.addToBattlefield(player1, new TurtlePower());

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The boost disappears when Turtle Power leaves the battlefield")
    void boostDisappearsWhenItLeavesBattlefield() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        int basePower = gqs.getEffectivePower(gd, turtle);
        int baseToughness = gqs.getEffectiveToughness(gd, turtle);
        harness.addToBattlefield(player1, new TurtlePower());

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness + 2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof TurtlePower);

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness);
    }
}
