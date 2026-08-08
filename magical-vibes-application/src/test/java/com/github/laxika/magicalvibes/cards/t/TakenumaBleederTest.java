package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CarnifexDemon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class TakenumaBleederTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without a Demon costs the controller 1 life")
    void attackingLosesLife() {
        addCreatureReady(player1, new TakenumaBleeder());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Attacking while controlling a Demon costs no life")
    void attackingWithDemonLosesNoLife() {
        addCreatureReady(player1, new TakenumaBleeder());
        addCreatureReady(player1, new CarnifexDemon());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Blocking without a Demon costs the controller 1 life")
    void blockingLosesLife() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TakenumaBleeder());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Staying home neither attacking nor blocking costs no life")
    void noCombatNoLifeLoss() {
        addCreatureReady(player1, new TakenumaBleeder());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
