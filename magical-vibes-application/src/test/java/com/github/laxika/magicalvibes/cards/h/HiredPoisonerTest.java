package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiredPoisoner.class, HillGiant.class})
class HiredPoisonerTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch kills a larger creature it damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent poisoner = addCreatureReady(player1, new HiredPoisoner());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        poisoner.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(poisoner);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(poisoner.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Deathtouch also kills a larger attacker when Hired Poisoner blocks")
    void deathtouchKillsLargerAttacker() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent poisoner = addCreatureReady(player2, new HiredPoisoner());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(poisoner.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed({HiredPoisoner.class})
    @DisplayName("Unblocked deathtouch damage only reduces the defending player's life")
    void unblockedDamageDoesNotDestroyPlayer() {
        Permanent poisoner = addCreatureReady(player1, new HiredPoisoner());
        poisoner.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(poisoner.getId()));
    }
}
