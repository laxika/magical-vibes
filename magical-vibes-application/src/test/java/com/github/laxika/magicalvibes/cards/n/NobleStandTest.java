package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class NobleStandTest extends BaseCardTest {

    @Test
    @DisplayName("You gain 2 life whenever a creature you control blocks")
    void ownCreatureBlocksGainsLife() {
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player1, new GrizzlyBears());
        addReady(player1, new NobleStand());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's blocking creature does not trigger Noble Stand")
    void opponentCreatureBlocksDoesNotGainLife() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new NobleStand());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each creature that blocks triggers Noble Stand separately")
    void eachBlockingCreatureTriggersSeparately() {
        Permanent attacker1 = addReady(player2, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addReady(player2, new GrizzlyBears());
        attacker2.setAttacking(true);
        addReady(player1, new GrizzlyBears());
        addReady(player1, new GrizzlyBears());
        addReady(player1, new NobleStand());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
