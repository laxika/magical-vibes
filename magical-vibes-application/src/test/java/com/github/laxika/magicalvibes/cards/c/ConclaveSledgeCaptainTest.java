package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConclaveSledgeCaptain.class, GrizzlyBears.class})
class ConclaveSledgeCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Three backup abilities each put a counter on another creature and grant the combat trigger")
    void backupTriggersSeparately() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castSledgeCaptain();

        resolveBackupTarget(bears);
        resolveBackupTarget(bears);
        resolveBackupTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        bears.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        // Bears is 5/5 after backup and each of the three granted triggers receives five counters.
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(18);
    }

    @Test
    @DisplayName("Conclave Sledge-Captain puts counters equal to combat damage on itself")
    void putsCountersEqualToCombatDamageOnItself() {
        Permanent captain = addCreatureReady(player1, new ConclaveSledgeCaptain());
        captain.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void backupGrantsTrampleToAnotherCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castSledgeCaptain();

        resolveBackupTarget(bears);
        resolveBackupTarget(bears);
        resolveBackupTarget(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void selfBackupDoesNotMultiplyPrintedCombatDamageAbility() {
        Permanent captain = castSledgeCaptain();
        resolveBackupTarget(captain);
        resolveBackupTarget(captain);
        resolveBackupTarget(captain);
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        captain.setSummoningSick(false);
        captain.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    void backupTargetsCanBeSplitBetweenSourceAndOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent captain = castSledgeCaptain();
        resolveBackupTarget(captain);
        resolveBackupTarget(bears);
        resolveBackupTarget(bears);

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        bears.setAttacking(true);
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    void grantedCombatDamageAbilitiesExpireButCountersRemain() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castSledgeCaptain();
        resolveBackupTarget(bears);
        resolveBackupTarget(bears);
        resolveBackupTarget(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        bears.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void backupStillGrantsCombatDamageAbilityAfterSourceDies() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent captain = castSledgeCaptain();
        harness.handlePermanentChosen(player1, bears.getId());
        captain.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Conclave Sledge-Captain");
        harness.passBothPriorities();
        resolveBackupTarget(bears);
        resolveBackupTarget(bears);

        bears.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(18);
    }

    private Permanent castSledgeCaptain() {
        harness.castFromHand(player1, new ConclaveSledgeCaptain(), "{5}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Conclave Sledge-Captain");
    }

    private void resolveBackupTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
