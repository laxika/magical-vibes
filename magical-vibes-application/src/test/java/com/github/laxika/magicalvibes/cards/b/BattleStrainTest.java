package com.github.laxika.magicalvibes.cards.b;

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

class BattleStrainTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a blocking creature's controller")
    void dealsDamageToBlockingCreaturesController() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers once for each blocking creature")
    void triggersOncePerBlocker() {
        Permanent attacker1 = addReady(player1, new HillGiant());
        attacker1.setAttacking(true);
        Permanent attacker2 = addReady(player1, new HillGiant());
        attacker2.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveStack();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declaring no blockers does not trigger Battle Strain")
    void noBlockersNoTrigger() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
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
