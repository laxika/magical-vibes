package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.p.ParanoidParishBlade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThaliasLieutenant.class, ParanoidParishBlade.class, DevilthornFox.class})
class ThaliasLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry puts a counter on each other Human you control, but not on itself")
    void entryCountersOtherHumans() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        harness.addToBattlefield(player1, new DevilthornFox());
        harness.castFromHand(player1, new ThaliasLieutenant(), "{1}{W}");
        resolveAllTriggers();

        Permanent lieutenant = findPermanent(player1, "Thalia's Lieutenant");
        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Devilthorn Fox")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another Human entering under your control puts a counter on it")
    void anotherHumanEnteringCountersLieutenant() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());
        harness.castFromHand(player1, new ParanoidParishBlade(), "{2}{W}");
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Human entering under an opponent's control does not trigger it")
    void opponentHumanEnteringDoesNotTrigger() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ParanoidParishBlade(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entryCountersEveryOtherControlledHumanButNoOpponentHuman() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ParanoidParishBlade());

        harness.castFromHand(player1, new ThaliasLieutenant(), "{1}{W}");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonHumanEnteringDoesNotTrigger() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());

        harness.castFromHand(player1, new DevilthornFox(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondLieutenantGivesFirstTwoCountersAndDoesNotCounterItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());

        harness.castFromHand(player1, new ThaliasLieutenant(), "{1}{W}");
        resolveAllTriggers();

        Permanent second = findPermanents(player1, "Thalia's Lieutenant").get(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void humanLeavingBeforeTriggerResolvesDoesNotPreventSourceCounter() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());
        harness.castFromHand(player1, new ParanoidParishBlade(), "{2}{W}");
        harness.passBothPriorities();
        Permanent human = findPermanent(player1, "Paranoid Parish-Blade");
        gd.playerBattlefields.get(player1.getId()).remove(human);
        gd.playerGraveyards.get(player1.getId()).add(human.getCard());

        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entryTriggerStillCountersOtherHumansAfterLieutenantLeaves() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        harness.castFromHand(player1, new ThaliasLieutenant(), "{1}{W}");
        harness.passBothPriorities();
        Permanent lieutenant = findPermanent(player1, "Thalia's Lieutenant");
        gd.playerBattlefields.get(player1.getId()).remove(lieutenant);
        gd.playerGraveyards.get(player1.getId()).add(lieutenant.getCard());

        resolveAllTriggers();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entryTriggerIncludesHumanThatEnteredAfterItTriggered() {
        harness.castFromHand(player1, new ThaliasLieutenant(), "{1}{W}");
        harness.passBothPriorities();
        Permanent human = harness.enterBattlefieldAndReturn(player1, new ParanoidParishBlade());

        resolveAllTriggers();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Thalia's Lieutenant")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
