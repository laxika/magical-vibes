package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloodcrazedNeonate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakishHeir.class, BloodcrazedNeonate.class, GrizzlyBears.class, SerraAngel.class})
class RakishHeirTest extends BaseCardTest {

    private Permanent addReadyRakishHeir() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new RakishHeir());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyVampire() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new BloodcrazedNeonate());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyNonVampire() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Another attacking Vampire gets a +1/+1 counter when dealing combat damage")
    void vampireGetsCounterOnCombatDamage() {
        addReadyRakishHeir();
        Permanent vampire = addReadyVampire();
        vampire.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage from Bloodcrazed Neonate
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve both triggered abilities (Neonate's own + Rakish Heir's)
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Vampire should have 2 +1/+1 counters (1 from own ability + 1 from Rakish Heir)
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Vampire creature does not trigger Rakish Heir")
    void nonVampireDoesNotTrigger() {
        addReadyRakishHeir();
        Permanent bears = addReadyNonVampire();
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage from Grizzly Bears
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // No triggered abilities to resolve — bears have no combat damage trigger, and Rakish Heir
        // doesn't trigger for non-Vampires
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rakish Heir triggers for itself when it deals combat damage")
    void triggersForItself() {
        Permanent heir = addReadyRakishHeir();
        heir.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve Rakish Heir's triggered ability
        harness.passBothPriorities();

        // Rakish Heir is a Vampire, so it should get a counter from its own trigger
        assertThat(heir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Rakish Heirs each trigger separately")
    void multipleRakishHeirs() {
        addReadyRakishHeir();
        addReadyRakishHeir();
        Permanent vampire = addReadyVampire();
        vampire.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Resolve all triggered abilities (Neonate's own + 2x Rakish Heir)
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Vampire should have 3 +1/+1 counters (1 from own ability + 2 from Rakish Heirs)
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("No counter when Vampire deals combat damage only to its blocker")
    void noCounterWhenVampireBlockedAndKilled() {
        addReadyRakishHeir();
        Permanent vampire = addReadyVampire();
        vampire.setAttacking(true);

        // 4/4 blocker kills the 2/1 Neonate (index 1 on attacker's battlefield)
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Vampire should be dead — no combat damage to player means no trigger
        harness.assertInGraveyard(player1, "Bloodcrazed Neonate");
    }

    @Test
    @DisplayName("Each Vampire dealing damage simultaneously gets its own counter")
    void simultaneousVampiresEachGetCounter() {
        Permanent heir = addReadyRakishHeir();
        Permanent first = addReadyVampire();
        Permanent second = addReadyVampire();
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(heir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opposing Vampire does not receive a counter from Rakish Heir")
    void opposingVampireDoesNotTriggerHeir() {
        Permanent heir = addReadyRakishHeir();
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new BloodcrazedNeonate());
        vampire.setSummoningSick(false);
        vampire.setAttacking(true);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(heir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
