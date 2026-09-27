package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinamoSchoolAtWatersEdge.class, KondaLordOfEiganjo.class, SakuraTribeElder.class})
class MinamoSchoolAtWatersEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds {U}")
    void manaAbilityAddsBlue() {
        harness.addToBattlefield(player1, new MinamoSchoolAtWatersEdge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Minamo, School at Water's Edge").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a target legendary permanent")
    void untapsLegendaryPermanent() {
        harness.addToBattlefield(player1, new MinamoSchoolAtWatersEdge());
        Permanent konda = harness.addToBattlefieldAndReturn(player1, new KondaLordOfEiganjo());
        konda.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, konda.getId());
        harness.passBothPriorities();

        assertThat(konda.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Minamo, School at Water's Edge").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's legendary permanent")
    void untapsOpponentLegendaryPermanent() {
        harness.addToBattlefield(player1, new MinamoSchoolAtWatersEdge());
        Permanent konda = harness.addToBattlefieldAndReturn(player2, new KondaLordOfEiganjo());
        konda.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, konda.getId());
        harness.passBothPriorities();

        assertThat(konda.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a legendary land")
    void untapsLegendaryLand() {
        Permanent minamo = harness.addToBattlefieldAndReturn(player1, new MinamoSchoolAtWatersEdge());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, minamo.getId());
        harness.passBothPriorities();

        assertThat(minamo.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonlegendary permanent")
    void cannotTargetNonlegendary() {
        harness.addToBattlefield(player1, new MinamoSchoolAtWatersEdge());
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
