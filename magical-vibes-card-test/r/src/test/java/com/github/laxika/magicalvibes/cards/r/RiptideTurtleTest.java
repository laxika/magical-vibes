package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiptideTurtle.class, NyxbornCourser.class})
class RiptideTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Riptide Turtle to be cast during an opponent's turn")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RiptideTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(RiptideTurtle.class);
    }

    @Test
    @DisplayName("Defender prevents Riptide Turtle from attacking")
    void cannotAttack() {
        Permanent turtle = addCreatureReady(player1, new RiptideTurtle());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(turtle.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A Turtle flashed in during combat can block immediately despite defender")
    void flashedInTurtleCanBlockImmediately() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new RiptideTurtle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.castCreature(player2, 0);
            harness.passBothPriorities();
        });

        harness.assertOnBattlefield(player2, "Riptide Turtle");
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Nyxborn Courser");
        harness.assertOnBattlefield(player2, "Riptide Turtle");
        assertThat(findPermanent(player2, "Riptide Turtle").getMarkedDamage()).isEqualTo(2);
    }
}
