package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpawnwritheTest extends BaseCardTest {

    private Permanent addReadySpawnwrithe() {
        Permanent perm = new Permanent(new Spawnwrithe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Creates a token copy of itself when dealing combat damage to a player")
    void createsTokenCopyOnCombatDamage() {
        Permanent spawnwrithe = addReadySpawnwrithe();
        spawnwrithe.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability -> token copy created
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Spawnwrithe"))
                .hasSize(2);
    }

    @Test
    @DisplayName("No token when blocked and killed without combat damage reaching the player")
    void noTokenWhenBlockedAndKilled() {
        Permanent spawnwrithe = addReadySpawnwrithe();
        spawnwrithe.setAttacking(true);
        harness.setLife(player2, 20);

        // 4/4 blocker soaks all trample damage (lethal = 4) and kills the 2/2 Spawnwrithe
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to combat damage (paused for trample assignment)

        // Trample can't deal lethal (4) to the blocker, so all 2 power goes to the blocker
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        // No combat damage to the player, so no token copy was created
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Spawnwrithe"))
                .isEmpty();
    }
}
