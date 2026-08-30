package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorthyKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistfordRiverTurtle.class, GrizzlyBears.class, WorthyKnight.class})
class MistfordRiverTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes another attacking non-Human creature unblockable")
    void makesAnotherAttackingNonHumanCreatureUnblockable() {
        addCreatureReady(player1, new MistfordRiverTurtle());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The attack trigger cannot target a Human creature")
    void cannotTargetHumanCreature() {
        addCreatureReady(player1, new MistfordRiverTurtle());
        Permanent nonHumanAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent humanAttacker = addCreatureReady(player1, new WorthyKnight());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, humanAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, nonHumanAttacker.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The attack trigger cannot target the Turtle itself")
    void cannotTargetItself() {
        Permanent turtle = addCreatureReady(player1, new MistfordRiverTurtle());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
