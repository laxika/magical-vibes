package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FarFortuneEndBossTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to each opponent")
    void attackingDealsDamageToEachOpponent() {
        addCreatureReady(player1, new FarFortuneEndBoss());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("At max speed, damage to an opponent is increased by 1")
    void maxSpeedIncreasesDamageToOpponent() {
        addCreatureReady(player1, new FarFortuneEndBoss());
        gd.playerSpeeds.put(player1.getId(), 4);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("At max speed, combat damage to an opponent's permanent is increased by 1")
    void maxSpeedIncreasesDamageToOpponentPermanent() {
        Permanent attacker = addCreatureReady(player1, new FarFortuneEndBoss());
        gd.playerSpeeds.put(player1.getId(), 4);
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }
}
