package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZimonesExperiment;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiaryOfDreams.class, Forest.class, GrizzlyBears.class, LightningBolt.class, ZimonesExperiment.class})
class DiaryOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a page counter on Diary of Dreams")
    void instantCastAddsPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the page-counter cast trigger

        assertThat(diary.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not add a page counter")
    void creatureCastAddsNoPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(diary.getCounterCount(CounterType.PAGE)).isZero();
    }

    @Test
    @DisplayName("With no page counters the ability costs {5}")
    void abilityCostsFiveWithNoCounters() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(diary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Three page counters reduce the ability to {2}")
    void threeCountersReduceCostToTwo() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate with less mana than the reduced cost")
    void cannotActivateWithoutEnoughReducedMana() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 3); // cost reduced to {2}
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1); // only {1}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("More page counters than the cost floor the ability at {0}")
    void countersBeyondCostFloorAtZero() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 8); // more than the {5} generic cost
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        // No mana added — {0} activation cost

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(diary.isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Casting a sorcery adds a page counter before the spell resolves")
    void sorceryCastAddsPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ZimonesExperiment(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(diary.getCounterCount(CounterType.PAGE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's instant does not add a page counter")
    void opponentInstantAddsNoPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(diary.getCounterCount(CounterType.PAGE)).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Non-page counters do not reduce the activation cost")
    void otherCountersDoNotReduceCost() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.CHARGE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(diary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A free draw activation still requires an untapped Diary")
    void cannotActivateTappedDiaryEvenWhenFree() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 5);
        diary.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The draw ability resolves after its source leaves the battlefield")
    void drawResolvesWithoutSource() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 5);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(diary);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A page-counter trigger cannot put counters on a departed source")
    void departedSourceGetsNoPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(diary);
        harness.passBothPriorities();

        assertThat(diary.getCounterCount(CounterType.PAGE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }
}
