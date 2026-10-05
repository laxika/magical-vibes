package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrizePig.class})
class PrizePigTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life puts that many ribbon counters on Prize Pig")
    void gainingLifePutsThatManyRibbonCountersOnPrizePig() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isEqualTo(2);
    }

    @Test
    @DisplayName("Three ribbon counters are removed and Prize Pig untaps")
    void threeRibbonCountersAreRemovedAndPigUntaps() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.tap();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prize Pig taps for a mana of any color")
    void tapsForAnyColor() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ribbon counters accumulate across life gains and untap only at three")
    void ribbonCountersAccumulateAcrossLifeGains() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.tap();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isEqualTo(2);
        assertThat(pig.isTapped()).isTrue();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exceeding three removes all ribbon counters but preserves other counters")
    void exceedingThresholdRemovesOnlyRibbonCounters() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        pig.setCounterCount(CounterType.RIBBON, 2);
        pig.tap();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(pig.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent gaining life does not add ribbon counters or untap Prize Pig")
    void opponentLifeGainDoesNotTrigger() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.tap();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(pig.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Prize Pig still removes ribbon counters at the threshold")
    void untappedPigStillRemovesRibbonCounters() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Life gain untaps Prize Pig so its mana ability can be used again")
    void lifeGainAllowsAnotherManaActivation() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(pig.isTapped()).isTrue();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(pig.isTapped()).isTrue();
        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
