package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatharsCrusade.class, GiantSpider.class, GrizzlyBears.class, GatherTheTownsfolk.class,
        Opalescence.class})
class CatharsCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature enters, puts a +1/+1 counter on each creature you control")
    void putsCountersOnAllOwnCreatures() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Crusade trigger

        Permanent entered = findPermanent(player1, "Grizzly Bears");

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not put counters on opponent creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void doesNotTriggerForOpponentCreatures() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple creature entries each put a counter on every creature")
    void stacksAcrossMultipleEntries() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent firstBears = findPermanent(player1, "Grizzly Bears");
        assertThat(firstBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firstBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent secondBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p != firstBears)
                .findFirst().orElseThrow();
        assertThat(secondBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Simultaneous token entries each trigger and each token receives both counters")
    void triggersForEachSimultaneouslyEnteringToken() {
        Permanent crusade = harness.addToBattlefieldAndReturn(player1, new CatharsCrusade());
        harness.castFromHand(player1, new GatherTheTownsfolk(), "{1}{W}");
        harness.passBothPriorities();

        var tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        assertThat(gd.stack).hasSize(2);
        assertThat(tokens).allSatisfy(p ->
                assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokens).allSatisfy(p ->
                assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(crusade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A pending trigger also gives a counter to a creature that entered afterward")
    void determinesAffectedCreaturesAtResolution() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent later = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(later.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(later.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cathars' Crusade triggers for its own entry when Opalescence makes it a creature")
    void triggersForItsOwnEntryAsACreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new CatharsCrusade(), "{3}{W}{W}");
        harness.passBothPriorities();

        Permanent crusade = findPermanent(player1, "Cathars' Crusade");
        assertThat(gqs.isCreature(gd, crusade)).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(crusade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
