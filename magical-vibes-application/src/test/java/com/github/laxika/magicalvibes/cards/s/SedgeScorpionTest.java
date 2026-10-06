package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SedgeScorpion.class, GrizzlyBears.class})
class SedgeScorpionTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch kills a larger creature it damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent scorpion = addCreatureReady(player2, new SedgeScorpion());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(scorpion);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(scorpion.getId()));
    }

    @Test
    @DisplayName("An attacking Scorpion destroys a larger blocker with deathtouch")
    void attackingScorpionKillsLargerBlocker() {
        Permanent scorpion = addCreatureReady(player1, new SedgeScorpion());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        scorpion.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scorpion);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sedge Scorpion");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Sedge Scorpion");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deathtouch does not make combat damage to a player lethal")
    void unblockedScorpionDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new SedgeScorpion());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Sedge Scorpion");
    }
}
