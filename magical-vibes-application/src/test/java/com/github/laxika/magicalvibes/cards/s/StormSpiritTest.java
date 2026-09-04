package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KjeldoranPhalanx;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormSpirit.class, BalduvianBears.class, Island.class, KjeldoranPhalanx.class})
class StormSpiritTest extends BaseCardTest {

    private Permanent addReadySpirit() {
        return addCreatureReady(player1, new StormSpirit());
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature, killing a 2/2")
    void killsTwoTwo() {
        addReadySpirit();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a larger creature without killing it")
    void damagesLargerCreature() {
        addReadySpirit();
        Permanent phalanx = harness.addToBattlefieldAndReturn(player2, new KjeldoranPhalanx());

        harness.activateAbility(player1, 0, null, phalanx.getId());
        harness.passBothPriorities();

        assertThat(phalanx.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires tap — cannot activate if already tapped")
    void cannotActivateIfTapped() {
        Permanent spirit = addReadySpirit();
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        spirit.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent spirit = addReadySpirit();

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadySpirit();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still deals damage if the source leaves before resolution")
    void stillDealsDamageIfSourceLeavesBeforeResolution() {
        Permanent spirit = addReadySpirit();
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }
}
