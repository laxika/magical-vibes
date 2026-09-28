package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConclaveSledgeCaptain.class, GrizzlyBears.class})
class ConclaveSledgeCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Three backup abilities each put a counter on another creature and grant the combat trigger")
    void backupTriggersSeparately() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
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

    private Permanent castSledgeCaptain() {
        harness.setHand(player1, List.of(new ConclaveSledgeCaptain()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Conclave Sledge-Captain");
    }

    private void resolveBackupTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
