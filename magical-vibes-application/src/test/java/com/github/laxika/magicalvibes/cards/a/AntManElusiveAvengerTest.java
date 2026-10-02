package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntManElusiveAvenger.class, GrizzlyBears.class, SuntailHawk.class})
class AntManElusiveAvengerTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithGreaterPower() {
        Permanent antMan = addCreatureReady(player1, new AntManElusiveAvenger());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(antMan);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    void canBeBlockedByCreatureWithEqualPower() {
        Permanent antMan = addCreatureReady(player1, new AntManElusiveAvenger());
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(antMan);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void createsTreasureTokensEqualToCombatDamageDealt() {
        Permanent antMan = addCreatureReady(player1, new AntManElusiveAvenger());
        antMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    void canBeBlockedByCreatureWithLessPowerAfterPowerIncrease() {
        Permanent antMan = addCreatureReady(player1, new AntManElusiveAvenger());
        antMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void blockedCombatDamageDoesNotCreateTreasure() {
        addCreatureReady(player1, new AntManElusiveAvenger());
        addCreatureReady(player2, new SuntailHawk());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void zeroPowerDoesNotDealDamageOrCreateTreasure() {
        Permanent antMan = addCreatureReady(player1, new AntManElusiveAvenger());
        antMan.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void treasureTriggerUsesDamageAmountAfterSourceLeavesBattlefield() {
        Permanent antMan = addCreatureReady(player1, new AntManElusiveAvenger());
        antMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        antMan.setAttacking(true);
        antMan.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        gd.playerBattlefields.get(player1.getId()).remove(antMan);
        gd.playerGraveyards.get(player1.getId()).add(antMan.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }
}
