package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WickerWarcrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives it a -1/-1 counter by the end of combat")
    void attackingPutsCounterAtEndOfCombat() {
        Permanent crawler = addCreatureReady(player1, new WickerWarcrawler());

        declareAttackers(player1, List.of(0));
        // No blockers exist, so priorities cascade through the combat damage step and out of
        // end of combat, draining the scheduled counter.
        harness.passBothPriorities();

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking puts a -1/-1 counter on it at end of combat")
    void blockingPutsCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent crawler = addCreatureReady(player2, new WickerWarcrawler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the block trigger

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        leaveEndOfCombat();

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when it neither attacks nor blocks")
    void noCounterWhenNotInCombat() {
        Permanent crawler = addCreatureReady(player1, new WickerWarcrawler());

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(PutMinusOneCounterAtEndOfCombat.class)).isFalse();

        leaveEndOfCombat();

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
