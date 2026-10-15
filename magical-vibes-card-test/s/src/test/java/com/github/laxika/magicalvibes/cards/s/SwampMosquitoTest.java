package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwampMosquito.class, FlyingMen.class})
class SwampMosquitoTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player1, new SwampMosquito());
    }

    @Test
    @DisplayName("Unblocked attacker gives the defending player a poison counter")
    void unblockedGivesPoison() {
        addAttacker();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void eachUnblockedAttackerGivesPoison() {
        addAttacker();
        addAttacker();

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only unblocked attackers give poison counters")
    void onlyUnblockedAttackersGivePoison() {
        addAttacker();
        addAttacker();
        addCreatureReady(player2, new FlyingMen());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked attacker gives no poison counter")
    void blockedNoPoison() {
        addCreatureReady(player2, new FlyingMen());

        addAttacker();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void poisonTriggersOnlyAfterBlockersAreDeclaredAndBeforeDamage() {
        addAttacker();

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of());
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
            resolveAllTriggers();
            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_BLOCKERS);
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
            assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        });
    }

    @Test
    void poisonStillResolvesAfterAttackerLeavesBattlefield() {
        Permanent mosquito = addAttacker();

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(mosquito);
        gd.playerGraveyards.get(player1.getId()).add(mosquito.getCard());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void playerTwoAttackingPoisonsPlayerOne() {
        addCreatureReady(player2, new SwampMosquito());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void tenthPoisonCounterMakesDefendingPlayerLose() {
        addAttacker();
        gd.playerPoisonCounters.put(player2.getId(), 9);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
