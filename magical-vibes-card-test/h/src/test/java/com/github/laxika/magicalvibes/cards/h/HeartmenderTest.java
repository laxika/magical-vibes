package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TripNoose;
import com.github.laxika.magicalvibes.cards.w.WiltLeafCavaliers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Heartmender.class, WiltLeafCavaliers.class, TripNoose.class})
class HeartmenderTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep removes one -1/-1 counter from each creature you control, clamping at zero")
    void upkeepRemovesOneMinusCounterFromEachControlledCreature() {
        harness.addToBattlefield(player1, new Heartmender());
        Permanent creatureA = harness.addToBattlefieldAndReturn(player1, new WiltLeafCavaliers());
        Permanent creatureB = harness.addToBattlefieldAndReturn(player1, new WiltLeafCavaliers());
        creatureA.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        creatureB.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(creatureA.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(creatureB.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Upkeep also removes a counter from Heartmender itself and leaves counterless creatures alone")
    void upkeepAffectsSelfAndSkipsCounterlessCreatures() {
        Permanent heartmender = harness.addToBattlefieldAndReturn(player1, new Heartmender());
        Permanent counterless = harness.addToBattlefieldAndReturn(player1, new WiltLeafCavaliers());
        heartmender.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(heartmender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(counterless.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Upkeep does not remove counters from creatures an opponent controls")
    void upkeepDoesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new Heartmender());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new WiltLeafCavaliers());
        opponentBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(opponentBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heartmender does not remove counters during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent heartmender = harness.addToBattlefieldAndReturn(player1, new Heartmender());
        heartmender.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(heartmender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep leaves counters on noncreature permanents alone")
    void upkeepDoesNotAffectNoncreatures() {
        harness.addToBattlefield(player1, new Heartmender());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TripNoose());
        artifact.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(artifact.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep affects creatures present on resolution even after Heartmender dies")
    void upkeepResolvesAfterSourceDiesAndIncludesNewCreatures() {
        Permanent heartmender = harness.addToBattlefieldAndReturn(player1, new Heartmender());
        heartmender.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        heartmender.setMarkedDamage(1);
        harness.runStateBasedActions();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new WiltLeafCavaliers());
        newcomer.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Heartmender");
        assertThat(newcomer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist returns Heartmender with a counter and upkeep enables it to persist again")
    void persistReturnsAndUpkeepAllowsAnotherReturn() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Heartmender());
        original.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Heartmender");
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Heartmender");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Heartmender");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        returned.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent returnedAgain = findPermanent(player1, "Heartmender");
        assertThat(returnedAgain).isNotNull();
        assertThat(returnedAgain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Heartmender");
    }

    @Test
    @DisplayName("Persist does not return Heartmender if it dies with a -1/-1 counter")
    void persistDoesNotReturnWithExistingCounter() {
        Permanent heartmender = harness.addToBattlefieldAndReturn(player1, new Heartmender());
        heartmender.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        heartmender.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Heartmender");
        harness.assertInGraveyard(player1, "Heartmender");
    }
}
