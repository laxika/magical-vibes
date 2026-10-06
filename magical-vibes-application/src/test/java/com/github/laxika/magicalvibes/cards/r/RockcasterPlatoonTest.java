package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RockcasterPlatoon.class, AngelOfMercy.class, GrizzlyBears.class, SuntailHawk.class})
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

    @Test
    @DisplayName("Also damages the controller's own flying creatures")
    void damagesOwnFlyers() {
        harness.addToBattlefield(player1, new RockcasterPlatoon());
        harness.addToBattlefield(player1, new AngelOfMercy());

        activate();

        assertThat(findPermanent(player1, "Angel of Mercy").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent platoon = harness.addToBattlefieldAndReturn(player1, new RockcasterPlatoon());
        platoon.tap();
        platoon.setSummoningSick(true);
        harness.addToBattlefield(player2, new SuntailHawk());

        activate();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(platoon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate repeatedly by paying the mana cost each time")
    void activatesRepeatedly() {
        harness.addToBattlefield(player1, new RockcasterPlatoon());
        harness.addToBattlefield(player2, new AngelOfMercy());

        activate();
        activate();

        harness.assertInGraveyard(player2, "Angel of Mercy");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        assertThat(findPermanent(player1, "Rockcaster Platoon").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Activated ability still deals damage after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        RockcasterPlatoon card = new RockcasterPlatoon();
        Permanent platoon = harness.addToBattlefieldAndReturn(player1, card);
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(platoon);
        harness.setGraveyard(player1, java.util.List.of(card));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Angel of Mercy").getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
