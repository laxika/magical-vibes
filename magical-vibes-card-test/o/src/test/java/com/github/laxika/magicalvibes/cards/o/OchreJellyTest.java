package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OchreJelly.class, PowerWordKill.class})
class OchreJellyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OchreJelly()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent jelly = findPermanent(player1, "Ochre Jelly");
        assertThat(jelly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates a delayed copy with half its death counters")
    void createsDelayedCopyWithHalfDeathCounters() {
        Permanent jelly = addCreatureReady(player1, new OchreJelly());
        jelly.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        destroyWithPowerWordKill(jelly);

        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).hasSize(1);
        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Ochre Jelly");
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().isToken()).isTrue();
        assertThat(copies.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger with fewer than two +1/+1 counters")
    void doesNotTriggerBelowCounterThreshold() {
        Permanent jelly = addCreatureReady(player1, new OchreJelly());
        jelly.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithPowerWordKill(jelly);

        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();
    }

    @Test
    @DisplayName("X zero dies without splitting")
    void zeroCountersDiesWithoutSplitting() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OchreJelly()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
    }

    @Test
    @DisplayName("Token copies split again and stop at one counter")
    void tokenCopiesSplitAgainAndStopAtOneCounter() {
        Permanent jelly = addCreatureReady(player1, new OchreJelly());
        jelly.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        destroyWithPowerWordKill(jelly);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent firstCopy = findPermanent(player1, "Ochre Jelly");
        assertThat(firstCopy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        destroyWithPowerWordKill(firstCopy);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Ochre Jelly");
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().isToken()).isTrue();
        assertThat(copies.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        destroyWithPowerWordKill(copies.getFirst());
        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();
    }

    @Test
    @DisplayName("Death during an end step waits until the next end step")
    void deathDuringEndStepWaitsUntilNextEndStep() {
        Permanent jelly = addCreatureReady(player1, new OchreJelly());
        jelly.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, jelly.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();
        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).hasSize(1);
        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Ochre Jelly");
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
    }

    private void destroyWithPowerWordKill(Permanent target) {
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }
}
