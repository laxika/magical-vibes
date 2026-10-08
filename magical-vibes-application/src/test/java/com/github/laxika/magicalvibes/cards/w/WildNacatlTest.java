package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildNacatl.class, Mountain.class, Plains.class, GrizzlyBears.class})
class WildNacatlTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 with no Mountain or Plains")
    void base() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 (2/2) with a Mountain")
    void withMountain() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 (2/2) with a Plains")
    void withPlains() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 (3/3) with both a Mountain and a Plains")
    void withBoth() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only one +1/+1 per land type (two Mountains still 2/2)")
    void twoMountainsStillSingleBoost() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Mountain does not grant the boost")
    void opponentMountainDoesNotCount() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-land creature does not grant the boost")
    void nonLandDoesNotCount() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Plains grant only one boost")
    void twoPlainsStillSingleBoost() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Plains does not grant the boost")
    void opponentPlainsDoesNotCount() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each boost disappears when the last corresponding land leaves")
    void boostsTrackLandsLeavingBattlefield() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(plains);
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Nacatl uses its own controller's lands")
    void boostsApplyOnlyToTheirSource() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new WildNacatl());
        Permanent opposingNacatl = harness.addToBattlefieldAndReturn(player2, new WildNacatl());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingNacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingNacatl)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
