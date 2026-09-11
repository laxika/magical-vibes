package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasCradle.class, CoralMerfolk.class})
class GaeasCradleTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gaea's Cradle adds one green mana for each creature you control")
    void addsGreenManaForEachControlledCreature() {
        harness.addToBattlefield(player1, new GaeasCradle());
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player2, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Gaea's Cradle with no creatures adds no mana")
    void addsNoManaWithNoControlledCreatures() {
        harness.addToBattlefield(player1, new GaeasCradle());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Counts tapped creatures and ignores noncreature permanents")
    void countsTappedCreaturesAndIgnoresNoncreatures() {
        harness.addToBattlefield(player1, new GaeasCradle());
        harness.addToBattlefield(player1, new GaeasCradle());
        Permanent tappedCreature = addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player2, new CoralMerfolk());

        tappedCreature.tap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
