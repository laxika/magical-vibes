package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodMoon.class, CityOfBrass.class, CoastalTower.class, Forest.class, Glimmerpost.class})
class BloodMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic land taps for red instead of its normal mana")
    void nonbasicLandProducesRed() {
        harness.addToBattlefield(player1, new CityOfBrass());
        harness.addToBattlefield(player1, new BloodMoon());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic land's subtypes are overridden to Mountain")
    void nonbasicLandSubtypesOverriddenToMountain() {
        Permanent cityOfBrass = harness.addToBattlefieldAndReturn(player1, new CityOfBrass());
        harness.addToBattlefield(player1, new BloodMoon());

        assertThat(gqs.effectiveLandTypes(gd, cityOfBrass)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Basic land is unaffected — still produces its normal mana")
    void basicLandUnaffected() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BloodMoon());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic land's subtypes are not overridden")
    void basicLandSubtypesNotOverridden() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new BloodMoon());

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Nonbasic land produces its normal mana once Blood Moon leaves")
    void normalManaResumesWhenBloodMoonLeaves() {
        harness.addToBattlefield(player1, new CoastalTower());
        Permanent bloodMoon = harness.addToBattlefieldAndReturn(player1, new BloodMoon());

        gd.playerBattlefields.get(player1.getId()).remove(bloodMoon);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic lands lose their printed triggered abilities")
    void nonbasicLandLosesPrintedTriggeredAbilities() {
        harness.addToBattlefield(player1, new BloodMoon());
        harness.addToBattlefield(player1, new CityOfBrass());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);

        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Nonbasic lands entering under Blood Moon do not trigger printed abilities")
    void nonbasicLandEnteringUnderBloodMoonDoesNotTriggerPrintedAbility() {
        harness.addToBattlefield(player1, new BloodMoon());
        harness.setHand(player1, List.of(new Glimmerpost()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Nonbasic lands controlled by an opponent also become Mountains")
    void opponentsNonbasicLandIsAffected() {
        harness.addToBattlefield(player1, new BloodMoon());
        harness.addToBattlefield(player2, new CityOfBrass());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }
}
