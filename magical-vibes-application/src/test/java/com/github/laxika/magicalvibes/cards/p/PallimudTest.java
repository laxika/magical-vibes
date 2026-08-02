package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PallimudTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of tapped lands the opponent controls; toughness stays 3")
    void powerCountsOpponentTappedLands() {
        Permanent pallimud = addPallimud();
        addOpponentLand(true);
        addOpponentLand(true);
        addOpponentLand(false);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
    }

    @Test
    @DisplayName("With no tapped opponent lands, power is 0")
    void noTappedLandsMeansZeroPower() {
        Permanent pallimud = addPallimud();
        addOpponentLand(false);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
    }

    @Test
    @DisplayName("Your own tapped lands don't count")
    void ignoresControllersOwnTappedLands() {
        Permanent pallimud = addPallimud();
        Permanent ownLand = new Permanent(new Mountain());
        ownLand.tap();
        gd.playerBattlefields.get(player1.getId()).add(ownLand);
        addOpponentLand(true);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates when an opponent land becomes tapped")
    void powerTracksLandsTappingLater() {
        Permanent pallimud = addPallimud();
        Permanent land = addOpponentLand(false);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isZero();

        land.tap();

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(1);
    }

    private Permanent addPallimud() {
        Permanent permanent = new Permanent(new Pallimud());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addOpponentLand(boolean tapped) {
        Permanent land = new Permanent(new Mountain());
        if (tapped) {
            land.tap();
        }
        gd.playerBattlefields.get(player2.getId()).add(land);
        return land;
    }
}
