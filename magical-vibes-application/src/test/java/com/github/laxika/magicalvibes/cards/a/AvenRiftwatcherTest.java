package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(AvenRiftwatcher.class)
class AvenRiftwatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters and gains 2 life")
    void entersWithCountersAndGainsLife() {
        harness.setHand(player1, List.of(new AvenRiftwatcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent riftwatcher = findPermanent(player1, "Aven Riftwatcher");
        assertThat(riftwatcher.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Removes a time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent riftwatcher = addCreatureReady(player1, new AvenRiftwatcher());
        riftwatcher.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(riftwatcher.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riftwatcher);
    }

    @Test
    @DisplayName("Sacrifices itself with no time counters and gains 2 life")
    void noTimeCountersSacrificesAndGainsLife() {
        addCreatureReady(player1, new AvenRiftwatcher());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aven Riftwatcher");
        harness.assertInGraveyard(player1, "Aven Riftwatcher");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
