package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalSliver.class, GaleriderSliver.class, GrizzlyBears.class})
class RegalSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Sliver ETBs make its controller the monarch when they are not the monarch")
    void sliverEntryMakesControllerMonarch() {
        addCreatureReady(player1, new RegalSliver());

        harness.enterBattlefieldAndReturn(player1, new GaleriderSliver());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Sliver ETBs pump your Slivers while you are the monarch")
    void sliverEntryPumpsYourSliversWhileMonarch() {
        Permanent regalSliver = addCreatureReady(player1, new RegalSliver());
        gd.monarchPlayerId = player1.getId();

        Permanent galerider = harness.enterBattlefieldAndReturn(player1, new GaleriderSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, regalSliver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, regalSliver)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, galerider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, galerider)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
