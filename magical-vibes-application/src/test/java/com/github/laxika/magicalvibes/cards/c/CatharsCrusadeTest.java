package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatharsCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature enters, puts a +1/+1 counter on each creature you control")
    void putsCountersOnAllOwnCreatures() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Crusade trigger

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple creature entries each put a counter on every creature")
    void stacksAcrossMultipleEntries() {
        harness.addToBattlefield(player1, new CatharsCrusade());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent firstBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(firstBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firstBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent secondBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p != firstBears)
                .findFirst().orElseThrow();
        assertThat(secondBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
