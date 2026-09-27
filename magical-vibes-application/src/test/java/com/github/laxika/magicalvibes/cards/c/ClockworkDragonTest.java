package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClockworkDragon.class})
class ClockworkDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with six +1/+1 counters")
    void entersWithCounters() {
        Permanent dragon = castDragon();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Activated ability puts a +1/+1 counter on it")
    void activatedAbilityAddsCounter() {
        Permanent dragon = castDragon();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Attacking removes a +1/+1 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent dragon = castDragon();
        dragon.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking keeps its counters through combat damage")
    void attackingKeepsCountersUntilEndOfCombat() {
        Permanent dragon = addReadyDragon(player1);
        Permanent blocker = addReadyDragon(player2);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking removes a +1/+1 counter at end of combat")
    void blockingRemovesCounterAtEndOfCombat() {
        Permanent attacker = addReadyDragon(player1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent dragon = addReadyDragon(player2);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not remove a counter when it neither attacks nor blocks")
    void doesNothingWhenNotInCombat() {
        Permanent dragon = addReadyDragon(player1);

        declareAttackers(player1, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private Permanent addReadyDragon(Player player) {
        Permanent dragon = addCreatureReady(player, new ClockworkDragon());
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        return dragon;
    }

    private Permanent castDragon() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ClockworkDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Clockwork Dragon");
    }
}
