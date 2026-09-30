package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredWastes;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheMoon.class, HorizonCanopy.class, SnowCoveredWastes.class})
class MagusOfTheMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic land taps for red instead of its normal mana")
    void nonbasicLandProducesRed() {
        harness.addToBattlefield(player1, new HorizonCanopy());
        harness.addToBattlefield(player1, new MagusOfTheMoon());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic land's subtypes are overridden to Mountain")
    void nonbasicLandSubtypesOverriddenToMountain() {
        Permanent horizonCanopy = harness.addToBattlefieldAndReturn(player1, new HorizonCanopy());
        harness.addToBattlefield(player1, new MagusOfTheMoon());

        assertThat(gqs.effectiveLandTypes(gd, horizonCanopy))
                .containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Basic land is unaffected and still produces its normal mana")
    void basicLandUnaffected() {
        harness.addToBattlefield(player1, new SnowCoveredWastes());
        harness.addToBattlefield(player1, new MagusOfTheMoon());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic land does not gain the Mountain subtype")
    void basicLandDoesNotGainMountainSubtype() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new SnowCoveredWastes());
        harness.addToBattlefield(player1, new MagusOfTheMoon());

        assertThat(gqs.effectiveLandTypes(gd, wastes)).isEmpty();
    }

    @Test
    @DisplayName("Nonbasic land controlled by an opponent taps for red")
    void opponentNonbasicLandProducesRed() {
        harness.addToBattlefield(player2, new HorizonCanopy());
        harness.addToBattlefield(player1, new MagusOfTheMoon());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic land loses its printed activated abilities")
    void nonbasicLandLosesPrintedActivatedAbilities() {
        harness.addToBattlefield(player1, new HorizonCanopy());
        harness.addToBattlefield(player1, new MagusOfTheMoon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nonbasic land resumes its printed mana ability after Magus of the Moon leaves")
    void normalManaResumesWhenMagusLeaves() {
        harness.addToBattlefield(player1, new HorizonCanopy());
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheMoon());
        harness.setLife(player1, 20);

        gd.playerBattlefields.get(player1.getId()).remove(magus);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        harness.assertLife(player1, 19);
    }
}
