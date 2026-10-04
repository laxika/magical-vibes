package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GuyInTheChair;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlyingOctobot.class, DocOcksHenchmen.class, GuyInTheChair.class})
class FlyingOctobotTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another Villain enters under your control")
    void getsCounterWhenAnotherVillainEnters() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Villain creature")
    void doesNotTriggerForNonVillain() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.setHand(player1, List.of(new GuyInTheChair()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Villain")
    void doesNotTriggerForOpponentsVillain() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DocOcksHenchmen()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.setHand(player1, List.of(new DocOcksHenchmen(), new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering itself does not use up the turn's trigger")
    void doesNotTriggerForItself() {
        Permanent octobot = harness.enterBattlefieldAndReturn(player1, new FlyingOctobot());

        assertThat(gd.stack).isEmpty();
        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Villain entering does not consume the once-per-turn trigger")
    void nonVillainDoesNotConsumeTrigger() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.enterBattlefieldAndReturn(player1, new GuyInTheChair());

        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The once-per-turn limit applies before the first trigger resolves")
    void secondEntryWhileTriggerIsPendingDoesNotTriggerAgain() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Octobot has its own once-per-turn limit")
    void separateOctobotsTriggerIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can trigger again during the opponent's next turn")
    void triggerLimitResetsOnOpponentsTurn() {
        Permanent octobot = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(octobot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
