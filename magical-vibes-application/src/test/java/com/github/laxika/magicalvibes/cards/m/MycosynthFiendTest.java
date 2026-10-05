package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MycosynthFiend.class})
class MycosynthFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/2 when no opponent has poison counters")
    void basePowerToughnessWithNoPoisonCounters() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new MycosynthFiend());

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 per poison counter on opponent")
    void boostsPerOpponentPoisonCounter() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new MycosynthFiend());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(5);
    }

    @Test
    @DisplayName("Controller's own poison counters do not boost")
    void controllerPoisonCountersDoNotCount() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new MycosynthFiend());
        gd.playerPoisonCounters.put(player1.getId(), 5);

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost updates when opponent gains more poison counters")
    void boostUpdatesDynamically() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new MycosynthFiend());

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);

        gd.playerPoisonCounters.put(player2.getId(), 2);
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(4);

        gd.playerPoisonCounters.put(player2.getId(), 7);
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(9);
    }

    @Test
    @DisplayName("Each Fiend counts only its own controller's opponents' poison")
    void opposingFiendsUseTheirRespectiveControllers() {
        Permanent firstFiend = harness.addToBattlefieldAndReturn(player1, new MycosynthFiend());
        Permanent secondFiend = harness.addToBattlefieldAndReturn(player2, new MycosynthFiend());
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 5);

        assertThat(gqs.getEffectivePower(gd, firstFiend)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, firstFiend)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, secondFiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondFiend)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost decreases immediately when opponent loses poison counters")
    void boostDecreasesWhenPoisonCountersAreRemoved() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new MycosynthFiend());
        gd.playerPoisonCounters.put(player2.getId(), 5);
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(7);

        gd.playerPoisonCounters.put(player2.getId(), 1);
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(3);

        gd.playerPoisonCounters.remove(player2.getId());
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(2);
    }
}
