package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtlePower.class, HornedTurtle.class, GrizzlyBears.class, Opalescence.class, MaskwoodNexus.class})
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

    @Test
    @DisplayName("Flash allows Turtle Power to resolve during an opponent's combat")
    void canCastDuringOpponentsCombat() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        int basePower = gqs.getEffectivePower(gd, turtle);
        int baseToughness = gqs.getEffectiveToughness(gd, turtle);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castFromHand(player1, new TurtlePower(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Turtle Power!");
        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Turtles entering after Turtle Power also receive its bonus")
    void buffsTurtlesEnteringLater() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        int basePower = gqs.getEffectivePower(gd, first);
        int baseToughness = gqs.getEffectiveToughness(gd, first);
        harness.addToBattlefield(player1, new TurtlePower());

        Permanent later = harness.enterBattlefieldAndReturn(player1, new HornedTurtle());

        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Multiple copies of Turtle Power give cumulative bonuses")
    void multipleCopiesStack() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        int basePower = gqs.getEffectivePower(gd, turtle);
        int baseToughness = gqs.getEffectiveToughness(gd, turtle);
        harness.addToBattlefield(player1, new TurtlePower());
        harness.addToBattlefield(player1, new TurtlePower());

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(baseToughness + 4);
    }

    @Test
    @CardUsed({Opalescence.class, MaskwoodNexus.class})
    @DisplayName("Turtle Power receives its own bonus when it becomes a Turtle creature")
    void buffsItselfWhenItIsATurtleCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent power = harness.addToBattlefieldAndReturn(player1, new TurtlePower());

        assertThat(gqs.getEffectivePower(gd, power)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, power)).isEqualTo(5);
    }
}
