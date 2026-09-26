package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AncientSpring;
import com.github.laxika.magicalvibes.cards.d.DarkwaterCatacombs;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasCradle;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Overabundance.class, Forest.class, MindStone.class, AncientSpring.class,
        DarkwaterCatacombs.class, GaeasCradle.class})
class OverabundanceTest extends BaseCardTest {

    @Test
    @DisplayName("Controller tapping a land adds mana and takes 1 damage")
    void controllerTappingLandGetsExtraManaAndTakesDamage() {
        harness.addToBattlefield(player1, new Overabundance());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Opponent tapping a land adds mana and takes 1 damage")
    void opponentTappingLandGetsExtraManaAndTakesDamage() {
        harness.addToBattlefield(player1, new Overabundance());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping a land for multiple mana types adds only one extra mana")
    void tappingLandForMultipleManaTypesAddsOnlyOneExtraMana() {
        harness.addToBattlefield(player1, new Overabundance());
        harness.addToBattlefield(player1, new DarkwaterCatacombs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.get(ManaColor.BLUE) + manaPool.get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A land sacrificed for its mana still grants Overabundance's bonus mana")
    void landSacrificedForManaStillGetsBonusMana() {
        harness.addToBattlefield(player1, new Overabundance());
        harness.addToBattlefield(player1, new AncientSpring());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, 1, null, null);

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.get(ManaColor.WHITE) + manaPool.get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping a land for zero mana still deals damage")
    void tappingLandForZeroManaStillDealsDamage() {
        harness.addToBattlefield(player1, new Overabundance());
        harness.addToBattlefield(player1, new GaeasCradle());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping a nonland mana source does not trigger Overabundance")
    void tappingNonlandManaSourceDoesNotTrigger() {
        harness.addToBattlefield(player1, new Overabundance());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
