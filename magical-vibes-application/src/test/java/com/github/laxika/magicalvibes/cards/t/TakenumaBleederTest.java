package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.s.ScourgeOfNumai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TakenumaBleeder.class, ScourgeOfNumai.class, FrostOgre.class})
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
        addCreatureReady(player1, new ScourgeOfNumai());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Attacking while the opponent controls a Demon still costs 1 life")
    void attackingWithOpponentsDemonStillLosesLife() {
        addCreatureReady(player1, new TakenumaBleeder());
        addCreatureReady(player2, new ScourgeOfNumai());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Blocking without a Demon costs the controller 1 life")
    void blockingLosesLife() {
        Permanent attacker = addCreatureReady(player1, new FrostOgre());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TakenumaBleeder());
        harness.setLife(player2, 20);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Blocking while controlling a Demon costs no life")
    void blockingWithDemonLosesNoLife() {
        Permanent attacker = addCreatureReady(player1, new FrostOgre());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TakenumaBleeder());
        addCreatureReady(player2, new ScourgeOfNumai());
        harness.setLife(player2, 20);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An attack trigger resolves using the Demon state at resolution")
    void attackTriggerChecksDemonAtResolution() {
        addCreatureReady(player1, new TakenumaBleeder());
        Permanent demon = addCreatureReady(player1, new ScourgeOfNumai());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, demon));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A block trigger resolves using the Demon state at resolution")
    void blockTriggerChecksDemonAtResolution() {
        Permanent attacker = addCreatureReady(player1, new FrostOgre());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TakenumaBleeder());
        Permanent demon = addCreatureReady(player2, new ScourgeOfNumai());
        harness.setLife(player2, 20);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, demon));
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
