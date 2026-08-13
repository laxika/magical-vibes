package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GaeasCradleTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gaea's Cradle adds one green mana for each creature you control")
    void addsGreenManaForEachControlledCreature() {
        harness.addToBattlefield(player1, new GaeasCradle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping Gaea's Cradle with no creatures adds no mana")
    void addsNoManaWithNoControlledCreatures() {
        harness.addToBattlefield(player1, new GaeasCradle());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
