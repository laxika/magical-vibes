package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiveNoGround;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranHomeGuard.class, GiveNoGround.class})
class KjeldoranHomeGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives it a -0/-1 counter and a Deserter token at end of combat")
    void attackingPutsCounterAndCreatesToken() {
        Permanent guard = addCreatureReady(player1, new KjeldoranHomeGuard());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(5);

        Permanent token = findPermanent(player1, "Deserter");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking gives it a -0/-1 counter and a Deserter token, but only at end of combat")
    void blockingPutsCounterAndCreatesToken() {
        Permanent attacker = addCreatureReady(player1, new KjeldoranHomeGuard());
        attacker.setAttacking(true);
        Permanent guard = addCreatureReady(player2, new KjeldoranHomeGuard());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the block trigger

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Deserter")).isEmpty();

        leaveEndOfCombat();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player2, "Deserter")).hasSize(1);
    }

    @Test
    @DisplayName("Blocking multiple creatures still gives only one counter and one Deserter token")
    void blockingMultipleCreaturesTriggersOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new KjeldoranHomeGuard());
        firstAttacker.setAttacking(true);
        firstAttacker.setAttackTarget(player2.getId());
        Permanent secondAttacker = addCreatureReady(player1, new KjeldoranHomeGuard());
        secondAttacker.setAttacking(true);
        secondAttacker.setAttackTarget(player2.getId());
        Permanent guard = addCreatureReady(player2, new KjeldoranHomeGuard());

        harness.setHand(player1, List.of(new GiveNoGround()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, guard.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Deserter")).isEmpty();

        leaveEndOfCombat();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player2, "Deserter")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing when it neither attacks nor blocks")
    void nothingWhenNotInCombat() {
        Permanent guard = addCreatureReady(player1, new KjeldoranHomeGuard());

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(PutCounterOnPermanentAtEndOfCombat.class)).isFalse();

        leaveEndOfCombat();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Deserter")).isEmpty();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
