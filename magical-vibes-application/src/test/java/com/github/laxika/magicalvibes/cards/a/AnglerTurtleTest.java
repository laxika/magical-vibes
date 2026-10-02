package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnglerTurtle.class, GrizzlyBears.class, Shock.class})
class AnglerTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents' creatures must attack each combat if able")
    void opponentCreaturesMustAttack() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The controller's creatures are not forced to attack")
    void controllerCreaturesAreNotForced() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Angler Turtle")
    void hexproofPreventsOpponentTargeting() {
        Permanent turtle = addCreatureReady(player1, new AnglerTurtle());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, turtle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Tapped opponents' creatures are not required to attack")
    void tappedCreatureIsNotForced() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick opponents' creatures are not required to attack")
    void summoningSickCreatureIsNotForced() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setSummoningSick(true);

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Every able opposing creature must attack")
    void cannotLeaveOneAbleCreatureBehind() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The attack requirement ends when Angler Turtle leaves the battlefield")
    void requirementEndsWhenTurtleLeaves() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new AnglerTurtle());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).remove(turtle);
        gd.playerGraveyards.get(player1.getId()).add(turtle.getCard());

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Hexproof allows Angler Turtle's controller to target it")
    void controllerCanTargetTurtle() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new AnglerTurtle());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, turtle.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(turtle.getId());
    }
}