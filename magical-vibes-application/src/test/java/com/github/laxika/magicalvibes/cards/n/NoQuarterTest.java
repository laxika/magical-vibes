package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoQuarterTest extends BaseCardTest {

    @Test
    @DisplayName("A blocker with lesser power than the attacker is destroyed")
    void weakerBlockerDestroyed() {
        Permanent attacker = addReady(player1, new HillGiant()); // 3/3
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears()); // 2/2
        addReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("An attacker with lesser power than the blocker is destroyed")
    void weakerAttackerDestroyed() {
        Permanent attacker = addReady(player1, new GrizzlyBears()); // 2/2
        attacker.setAttacking(true);
        addReady(player2, new HillGiant()); // 3/3
        addReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Equal power on both sides destroys neither creature")
    void equalPowerDestroysNothing() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No Quarter watches combat between creatures its controller doesn't control")
    void triggersForOpponentControlledCombat() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
