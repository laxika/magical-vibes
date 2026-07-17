package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RockcasterPlatoonTest extends BaseCardTest {

    private void activate() {
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage to each creature with flying, sparing non-flyers")
    void damagesFlyersOnly() {
        harness.addToBattlefield(player1, new RockcasterPlatoon());
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        activate();

        assertThat(findPermanent(player2, "Angel of Mercy").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kills a 1-toughness flyer")
    void killsSmallFlyer() {
        harness.addToBattlefield(player1, new RockcasterPlatoon());
        harness.addToBattlefield(player2, new SuntailHawk());

        activate();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Deals 2 damage to each player, including its controller")
    void damagesBothPlayers() {
        harness.addToBattlefield(player1, new RockcasterPlatoon());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        activate();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
    }
}
