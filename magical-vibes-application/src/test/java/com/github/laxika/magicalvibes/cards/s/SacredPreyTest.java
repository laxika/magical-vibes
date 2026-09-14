package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SacredPrey.class, FreshVolunteers.class})
class SacredPreyTest extends BaseCardTest {

    @Test
    @DisplayName("When Sacred Prey becomes blocked, its controller gains 1 life")
    void becomesBlockedGainsLife() {
        harness.setLife(player1, 20);
        addAttackingPrey(player1, player2);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures causes only one life gain")
    void multipleBlockersGainLifeOnlyOnce() {
        harness.setLife(player1, 20);
        addAttackingPrey(player1, player2);
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An unblocked Sacred Prey does not gain life")
    void unblockedDoesNotGainLife() {
        harness.setLife(player1, 20);
        addAttackingPrey(player1, player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private Permanent addAttackingPrey(Player attacker, Player defender) {
        Permanent permanent = addCreatureReady(attacker, new SacredPrey());
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
