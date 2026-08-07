package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        mirri.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, mirri.getId());
        harness.passBothPriorities();

        assertThat(mirri.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Minamo, School at Water's Edge").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's legendary permanent")
    void untapsOpponentLegendaryPermanent() {
        harness.addToBattlefield(player1, new MinamoSchoolAtWatersEdge());
        Permanent mirri = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());
        mirri.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, mirri.getId());
        harness.passBothPriorities();

        assertThat(mirri.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonlegendary permanent")
    void cannotTargetNonlegendary() {
        harness.addToBattlefield(player1, new MinamoSchoolAtWatersEdge());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
