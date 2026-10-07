package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spawnwrithe.class, SerraAngel.class})
class SpawnwritheTest extends BaseCardTest {

    private Permanent addReadySpawnwrithe() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new Spawnwrithe());
        perm.setSummoningSick(false);
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
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

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

    @Test
    @DisplayName("Token copies inherit the combat damage trigger but not counters or attacking status")
    void tokenCopyCanCreateAnotherCopy() {
        Permanent source = addReadySpawnwrithe();
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        source.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();

        source.setAttacking(false);
        token.setSummoningSick(false);
        token.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Trample damage creates a copy even when the source dies in the same combat")
    void createsCopyAfterSourceDiesInCombat() {
        Permanent source = addReadySpawnwrithe();
        source.setPowerModifier(3);
        source.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Spawnwrithe");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        token.setSummoningSick(false);
        token.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
