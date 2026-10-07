package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BruteForce;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tidewalker.class, Island.class, Timebender.class, BruteForce.class})
class TidewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with time counters equal to the Islands its controller controls")
    void entersWithIslandCountTimeCounters() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Tidewalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent tidewalker = findPermanent(player1, "Tidewalker");
        assertThat(tidewalker.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dies as a 0/0 when its controller controls no Islands")
    void diesAsZeroToughnessWithoutIslands() {
        harness.setHand(player1, List.of(new Tidewalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tidewalker");
        harness.assertInGraveyard(player1, "Tidewalker");
    }

    @Test
    @DisplayName("Power and toughness track its time counters")
    void powerAndToughnessTrackTimeCounters() {
        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.TIME, 2);

        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(tidewalker.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vanishing triggers only during its controller's upkeep")
    void doesNotRemoveCounterDuringOpponentsUpkeep() {
        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player2);

        assertThat(tidewalker.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void sacrificesOnLastTimeCounter() {
        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tidewalker");
        harness.assertInGraveyard(player1, "Tidewalker");
    }

    @Test
    @DisplayName("Removing the last time counter with Timebender triggers sacrifice")
    void externalLastCounterRemovalTriggersSacrifice() {
        harness.setHand(player1, List.of(new Timebender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent timebender = findPermanent(player1, "Timebender");

        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new BruteForce()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, tidewalker.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(timebender));
        harness.handleListChoice(player1, "Remove two time counters");
        harness.handlePermanentChosen(player1, tidewalker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tidewalker");
        harness.assertInGraveyard(player1, "Tidewalker");
    }

    @Test
    @DisplayName("Vanishing does not trigger upkeep removal without a time counter")
    void noUpkeepTriggerWithoutTimeCounters() {
        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tidewalker");
    }

    @Test
    @DisplayName("Island count is fixed on entry while power follows time counters")
    void laterIslandsDoNotChangeTimeCountersOrPower() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Tidewalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent tidewalker = findPermanent(player1, "Tidewalker");
        harness.addToBattlefield(player1, new Island());

        assertThat(tidewalker.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(1);
    }
}

