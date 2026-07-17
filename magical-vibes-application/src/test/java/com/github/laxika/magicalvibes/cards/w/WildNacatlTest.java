package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WildNacatlTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 with no Mountain or Plains")
    void base() {
        harness.addToBattlefield(player1, new WildNacatl());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 (2/2) with a Mountain")
    void withMountain() {
        harness.addToBattlefield(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Mountain());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 (2/2) with a Plains")
    void withPlains() {
        harness.addToBattlefield(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Plains());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 (3/3) with both a Mountain and a Plains")
    void withBoth() {
        harness.addToBattlefield(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only one +1/+1 per land type (two Mountains still 2/2)")
    void twoMountainsStillSingleBoost() {
        harness.addToBattlefield(player1, new WildNacatl());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Mountain does not grant the boost")
    void opponentMountainDoesNotCount() {
        harness.addToBattlefield(player1, new WildNacatl());
        harness.addToBattlefield(player2, new Mountain());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-land creature does not grant the boost")
    void nonLandDoesNotCount() {
        harness.addToBattlefield(player1, new WildNacatl());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent nacatl = findPermanent(player1, "Wild Nacatl");
        assertThat(gqs.getEffectivePower(gd, nacatl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nacatl)).isEqualTo(1);
    }
}
