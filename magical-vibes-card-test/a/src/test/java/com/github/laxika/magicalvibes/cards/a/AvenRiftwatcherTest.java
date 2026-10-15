package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeadGone;
import com.github.laxika.magicalvibes.cards.t.Timecrafting;
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

@CardUsed({AvenRiftwatcher.class, Timecrafting.class, DeadGone.class})
class AvenRiftwatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters and gains 2 life")
    void entersWithCountersAndGainsLife() {

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AvenRiftwatcher(), "{2}{W}");
        resolveAllTriggers();

        Permanent riftwatcher = findPermanent(player1, "Aven Riftwatcher");
        assertThat(riftwatcher.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Removes a time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent riftwatcher = addCreatureReady(player1, new AvenRiftwatcher());
        riftwatcher.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(riftwatcher.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riftwatcher);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed and gains 2 life")
    void lastTimeCounterCausesSacrificeAndGainsLife() {
        Permanent riftwatcher = addCreatureReady(player1, new AvenRiftwatcher());
        riftwatcher.setCounterCount(CounterType.TIME, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aven Riftwatcher");
        harness.assertInGraveyard(player1, "Aven Riftwatcher");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Does not sacrifice itself when it has no time counters")
    void noTimeCountersDoesNotSacrifice() {
        addCreatureReady(player1, new AvenRiftwatcher());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aven Riftwatcher");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void removingLastTimeCounterWithTimecraftingSacrificesAndGainsLife() {
        harness.castFromHand(player1, new AvenRiftwatcher(), "{2}{W}");
        resolveAllTriggers();
        Permanent riftwatcher = findPermanent(player1, "Aven Riftwatcher");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castModalInstantForX(player1, 0, 0, 3, riftwatcher.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aven Riftwatcher");
        harness.assertInGraveyard(player1, "Aven Riftwatcher");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void opponentsUpkeepDoesNotRemoveTimeCounters() {
        Permanent riftwatcher = addCreatureReady(player1, new AvenRiftwatcher());
        riftwatcher.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(riftwatcher.getCounterCount(CounterType.TIME)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Aven Riftwatcher");
    }

    @Test
    void returningToHandGainsLifeForTheCreaturesController() {
        Permanent riftwatcher = addCreatureReady(player2, new AvenRiftwatcher());
        riftwatcher.setCounterCount(CounterType.TIME, 3);
        int controllerLife = gd.playerLifeTotals.get(player2.getId());
        int casterLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstant(player1, 0, 1, List.of(riftwatcher.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Aven Riftwatcher");
        harness.assertInHand(player2, "Aven Riftwatcher");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLife + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLife);
    }
}
