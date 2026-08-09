package com.github.laxika.magicalvibes.cards.h;

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

class HeatOfBattleTest extends BaseCardTest {

    @Test
    @DisplayName("A blocking creature's controller is dealt 1 damage")
    void blockerControllerTakesDamage() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each blocking creature triggers separately")
    void triggersOncePerBlocker() {
        Permanent attacker1 = addReady(player1, new HillGiant());
        attacker1.setAttacking(true);
        Permanent attacker2 = addReady(player1, new HillGiant());
        attacker2.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        addReady(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveStack();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The trigger is symmetric when its controller's creature blocks")
    void ownControllerTakesDamageWhenBlocking() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No blockers means no damage")
    void noBlockersNoDamage() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 17);
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
