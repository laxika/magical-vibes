package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClearwaterGoblet.class})
class ClearwaterGobletTest extends BaseCardTest {

    @Test
    void sunburstCanPutFiveChargeCounters() {
        harness.setHand(player1, List.of(new ClearwaterGoblet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clearwater Goblet").getCounterCount(CounterType.CHARGE))
                .isEqualTo(5);
    }

    @Test
    void upkeepWithNoChargeCountersGainsNoLife() {
        harness.addToBattlefield(player1, new ClearwaterGoblet());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void upkeepCountsOnlyChargeCountersAndDoesNotConsumeThem() {
        Permanent goblet = harness.addToBattlefieldAndReturn(player1, new ClearwaterGoblet());
        goblet.setCounterCount(CounterType.CHARGE, 2);
        goblet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 2);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(goblet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(goblet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new ClearwaterGoblet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent goblet = findPermanent(player1, "Clearwater Goblet");
        assertThat(goblet.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new ClearwaterGoblet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent goblet = findPermanent(player1, "Clearwater Goblet");
        assertThat(goblet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void sunburstIgnoresColorlessMana() {
        harness.setHand(player1, List.of(new ClearwaterGoblet()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent goblet = findPermanent(player1, "Clearwater Goblet");
        assertThat(goblet.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void mayGainLifeEqualToChargeCountersDuringUpkeep() {
        Permanent goblet = harness.addToBattlefieldAndReturn(player1, new ClearwaterGoblet());
        goblet.setCounterCount(CounterType.CHARGE, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void mayGainLifeUsesCountersAtResolution() {
        Permanent goblet = harness.addToBattlefieldAndReturn(player1, new ClearwaterGoblet());
        goblet.setCounterCount(CounterType.CHARGE, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        goblet.setCounterCount(CounterType.CHARGE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent goblet = harness.addToBattlefieldAndReturn(player1, new ClearwaterGoblet());
        goblet.setCounterCount(CounterType.CHARGE, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineUpkeepLifeGain() {
        Permanent goblet = harness.addToBattlefieldAndReturn(player1, new ClearwaterGoblet());
        goblet.setCounterCount(CounterType.CHARGE, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
