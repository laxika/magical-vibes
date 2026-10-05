package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KhabLGhoul.class, GrizzlyBears.class, Terror.class})
class KhabLGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Gets one +1/+1 counter for each creature that died this turn")
    void getsCountersForCreaturesThatDiedThisTurn() {
        Permanent ghoul = addCreatureReady(player1, new KhabLGhoul());
        Permanent firstVictim = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondVictim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terror(), new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, firstVictim.getId());
        harness.castAndResolveInstant(player1, 0, secondVictim.getId());
        advanceToEndStepAndResolve(player2);

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets no counter when no creature died this turn")
    void getsNoCounterWithoutCreatureDeath() {
        Permanent ghoul = addCreatureReady(player1, new KhabLGhoul());

        advanceToEndStepAndResolve(player1);

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts creatures controlled by its controller as well as opponents")
    void countsDeathsOnBothBattlefields() {
        Permanent ghoul = addCreatureReady(player1, new KhabLGhoul());
        Permanent ownVictim = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingVictim = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror(), new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, ownVictim.getId());
        harness.castAndResolveInstant(player1, 0, opposingVictim.getId());
        advanceToEndStepAndResolve(player1);

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts creatures that died before it entered the battlefield")
    void countsDeathsBeforeEntering() {
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        Permanent ghoul = addCreatureReady(player1, new KhabLGhoul());

        advanceToEndStepAndResolve(player1);

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a death in response even when no creature died before the end step")
    void countsDeathsWhileTriggerIsOnStack() {
        Permanent ghoul = addCreatureReady(player1, new KhabLGhoul());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror()));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        resolveAllTriggers();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
