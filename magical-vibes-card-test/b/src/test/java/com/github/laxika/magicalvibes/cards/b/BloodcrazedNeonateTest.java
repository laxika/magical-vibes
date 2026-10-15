package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({BloodcrazedNeonate.class, GrizzlyBears.class, SerraAngel.class})
class BloodcrazedNeonateTest extends BaseCardTest {

    private Permanent addReadyNeonate() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new BloodcrazedNeonate());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Declaring no attackers when Bloodcrazed Neonate can attack throws exception")
    void mustAttackWhenAble() {
        addReadyNeonate();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Bloodcrazed Neonate does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new BloodcrazedNeonate());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent neonate = addReadyNeonate();
        neonate.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Neonate should have a +1/+1 counter
        assertThat(neonate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting a +1/+1 counter")
    void dealsMoreDamageWithCounter() {
        Permanent neonate = addReadyNeonate();
        neonate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // simulate having gotten a counter previously
        neonate.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 2 base power + 1 from counter = 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        // Resolve trigger — gets another counter
        harness.passBothPriorities();
        assertThat(neonate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when blocked and killed")
    void noCounterWhenBlockedAndKilled() {
        Permanent neonate = addReadyNeonate();
        neonate.setAttacking(true);

        // 4/4 blocker kills the 2/1 Neonate
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Neonate should be dead
        harness.assertInGraveyard(player1, "Bloodcrazed Neonate");
    }

    @Test
    @DisplayName("A tapped Bloodcrazed Neonate is not required to attack")
    void tappedNeonateDoesNotHaveToAttack() {
        Permanent neonate = addReadyNeonate();
        neonate.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        harness.assertLife(player2, 18);
        assertThat(neonate.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a blocker does not add a counter even when Neonate survives")
    void survivingBlockedNeonateDoesNotGetCounter() {
        Permanent neonate = addReadyNeonate();
        neonate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        neonate.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Bloodcrazed Neonate");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(neonate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
