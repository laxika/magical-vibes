package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClockworkVorracTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithCounters() {
        Permanent vorrac = castVorrac();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tapped ability puts a +1/+1 counter on it")
    void activatedAbilityAddsCounter() {
        Permanent vorrac = castVorrac();
        vorrac.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(vorrac.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking removes a +1/+1 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent vorrac = addCreatureReady(player1, new ClockworkVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocking removes a +1/+1 counter at end of combat")
    void blockingRemovesCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent vorrac = addCreatureReady(player2, new ClockworkVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent castVorrac() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ClockworkVorrac()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Clockwork Vorrac");
    }
}
