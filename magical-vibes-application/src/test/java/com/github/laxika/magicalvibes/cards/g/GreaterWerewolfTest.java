package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreaterWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("When Greater Werewolf becomes blocked, each blocker is scheduled for a -0/-2 counter")
    void becomesBlockedSchedulesCounter() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        werewolf.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider()); // 2/4

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the becomes-blocked trigger

        assertThat(gd.getDelayedActions(PutCounterOnPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId())
                        && a.counterType() == CounterType.MINUS_ZERO_MINUS_TWO);
    }

    @Test
    @DisplayName("The blocker gets a -0/-2 counter reducing its toughness at end of combat")
    void blockerToughnessReducedAtEndOfCombat() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        werewolf.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider()); // 2/4

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the becomes-blocked trigger

        assertThat(spider.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Greater Werewolf blocks an attacker, that attacker is scheduled for a -0/-2 counter")
    void blocksAttackerSchedulesCounter() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider()); // 2/4
        attacker.setAttacking(true);
        addCreatureReady(player2, new GreaterWerewolf());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the block trigger

        assertThat(gd.getDelayedActions(PutCounterOnPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId())
                        && a.counterType() == CounterType.MINUS_ZERO_MINUS_TWO);
    }

    @Test
    @DisplayName("Does nothing when Greater Werewolf neither blocks nor is blocked")
    void noCounterWhenNotInCombat() {
        addCreatureReady(player1, new GreaterWerewolf());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(PutCounterOnPermanentAtEndOfCombat.class)).isFalse();
        assertThat(spider.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
