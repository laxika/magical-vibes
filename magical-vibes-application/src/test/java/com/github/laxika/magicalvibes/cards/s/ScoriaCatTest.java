package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoriaCatTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 when its controller controls no untapped lands")
    void getsBoostWithNoUntappedLands() {
        Permanent cat = addCat();

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not get the boost while its controller controls an untapped land")
    void noBoostWithUntappedLand() {
        Permanent cat = addCat();
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets the boost when its controller's only land is tapped")
    void getsBoostWithOnlyTappedLand() {
        Permanent cat = addCat();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.tap();

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ignores untapped lands controlled by an opponent")
    void ignoresOpponentsUntappedLand() {
        Permanent cat = addCat();
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost changes as lands become tapped or untapped")
    void boostChangesWithLandStatus() {
        Permanent cat = addCat();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);

        land.tap();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(6);

        land.untap();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
    }

    private Permanent addCat() {
        harness.addToBattlefield(player1, new ScoriaCat());
        return findPermanent(player1, "Scoria Cat");
    }
}
