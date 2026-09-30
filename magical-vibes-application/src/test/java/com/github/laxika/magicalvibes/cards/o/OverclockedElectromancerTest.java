package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverclockedElectromancer.class, HillGiant.class})
class OverclockedElectromancerTest extends BaseCardTest {

    @Test
    void mayPayEnergyAtBeginningOfCombatForCounter() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(electromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningBeginningOfCombatPaymentLeavesCreatureUnchanged() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(electromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doublesPowerUntilEndOfTurnWhenItAttacks() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, electromancer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, electromancer)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, electromancer)).isEqualTo(2);
    }

    @Test
    void gainsEnergyEqualToCombatExcessDamage() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());
        hillGiant.addMarkedDamage(null, 2);

        electromancer.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(electromancer);
    }

    @Test
    void doesNotGainEnergyWithoutExcessDamage() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());
        addCreatureReady(player2, new HillGiant());

        electromancer.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(electromancer);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
